package com.firstcoupon.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.firstcoupon.domain.Coupon;
import com.firstcoupon.domain.CouponStatus;
import com.firstcoupon.domain.IssuedCoupon;
import com.firstcoupon.dto.CouponIssue;
import com.firstcoupon.exception.CouponAlreadyUsed;
import com.firstcoupon.exception.NotIssuableTime;
import com.firstcoupon.repository.CouponRepository;
import com.firstcoupon.repository.IssuedCouponRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.Random.class)
class CouponServiceTest {

    private static final int THREAD_COUNT = 500;
    private static final int TOTAL_USERS = 10_000;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private IssuedCouponRepository issuedCouponRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    //@AfterEach
    //void tearDown() {
    //    couponRepository.deleteAll();
    //}

    @BeforeEach
    void clean() {
        issuedCouponRepository.deleteAll();
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    private Coupon getCoupon() {
        Coupon coupon = Coupon.create(
                "테스트 쿠폰" + new Random().nextInt(TOTAL_USERS),
                1_000,
                LocalDate.now().plusDays(7),
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1));
        couponRepository.save(coupon);
        return coupon;
    }

    @Test
    void 쿠폰을_1개_발급한다() {
        //given
        CouponIssue couponIssue = new CouponIssue(getCoupon().getCode(), "test@test.com");

        //when
        couponService.issueCoupon(couponIssue);

        //then
        assertEquals(1L, issuedCouponRepository.count());
    }

    @Test
    void 쿠폰은_발급_가능한_시간에만_발급할_수있다() {
        //given
        //쿠폰 발급 가능 시간을 과거로 설정
        Coupon coupon = Coupon.create("테스트", 100, LocalDate.now().plusDays(7),
                LocalDateTime.now().minusMinutes(60), LocalDateTime.now().minusMinutes(30));
        couponRepository.save(coupon);

        CouponIssue couponIssue = new CouponIssue(coupon.getCode(), "test@test.com");

        //expected
        assertThrows(NotIssuableTime.class, () -> couponService.issueCoupon(couponIssue));
    }

    @Test
    @Transactional
    void 발급받은_쿠폰을_사용한다() {
        //given
        Coupon coupon = couponRepository.findAll().get(0);
        String email = "test@test.com";
        IssuedCoupon issuedCoupon = IssuedCoupon.issue(email, coupon);
        issuedCouponRepository.save(issuedCoupon);

        //when
        couponService.useCoupon(email);

        //then
        assertEquals(CouponStatus.USED, issuedCoupon.getStatus());
        assertNotNull(issuedCoupon.getUsedAt());
    }

    @Test
    @Transactional
    void 발급받은_쿠폰을_재사용할_수없다() {
        //given
        Coupon coupon = couponRepository.findAll().get(0);
        String email = "test@test.com";
        IssuedCoupon issuedCoupon = IssuedCoupon.issue(email, coupon);
        issuedCouponRepository.save(issuedCoupon);

        couponService.useCoupon(email);

        //expected
        assertThrows(CouponAlreadyUsed.class, () -> couponService.useCoupon(email));
    }

    @Test
    void synchronized를_사용하여_동시에_총_발급_수량까지만_발급된다() throws InterruptedException {
        //given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        Coupon coupon = getCoupon();
        String code = coupon.getCode();
        int totalQuantity = coupon.getTotalQuantity();

        //when
        for (int i = 1; i <= TOTAL_USERS; i++) {
            String email = "test" + i + "@test.com";
            CouponIssue couponIssue = new CouponIssue(code, email);
            executorService.execute(() -> {
                try {
                    couponService.issueCouponWithSynchronized(couponIssue);
                } catch (Exception ignored) {
                }
                latch.countDown();
            });
        }

        latch.await();
        executorService.shutdown();

        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> issuedCouponRepository.count() == totalQuantity);

        //then
        assertEquals(totalQuantity, issuedCouponRepository.count());
    }

    @Test
    void Redis를_사용하여_동시에_총_발급_수량까지만_발급된다() throws InterruptedException {
        //given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        Coupon coupon = getCoupon();
        String code = coupon.getCode();
        int totalQuantity = coupon.getTotalQuantity();

        //when
        for (int i = 1; i <= TOTAL_USERS; i++) {
            String email = "test" + i + "@test.com";
            CouponIssue couponIssue = new CouponIssue(code, email);
            executorService.execute(() -> {
                try {
                    couponService.issueCouponWithRedis(couponIssue);
                } catch (Exception ignored) {
                }
                latch.countDown();
            });
        }

        latch.await();
        executorService.shutdown();

        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> issuedCouponRepository.count() == totalQuantity);

        //then
        assertEquals(totalQuantity, issuedCouponRepository.count());
    }

    @Test
    void 분산락을_사용하여_동시에_총_발급_수량까지만_발급된다() throws InterruptedException {
        //given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        Coupon coupon = getCoupon();
        String code = coupon.getCode();
        int totalQuantity = coupon.getTotalQuantity();

        //when
        for (int i = 1; i <= TOTAL_USERS; i++) {
            String email = "test" + i + "@test.com";
            CouponIssue couponIssue = new CouponIssue(code, email);
            executorService.execute(() -> {
                try {
                    couponService.issueCouponWithRedisson(couponIssue);
                } catch (Exception ignored) {
                }
                latch.countDown();
            });
        }

        latch.await();
        executorService.shutdown();

        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> issuedCouponRepository.count() == totalQuantity);

        //then
        assertEquals(totalQuantity, issuedCouponRepository.count());
    }

    @Test
    void Kafka를_사용하여_동시에_총_발급_수량까지만_발급된다() throws InterruptedException {
        //given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        Coupon coupon = getCoupon();
        String code = coupon.getCode();
        int totalQuantity = coupon.getTotalQuantity();

        //when
        for (int i = 1; i <= TOTAL_USERS; i++) {
            String email = "test" + i + "@email.com";
            CouponIssue couponIssue = new CouponIssue(code, email);
            executorService.execute(() -> {
                try {
                    couponService.issueCouponWithKafka(couponIssue);
                } catch (Exception ignored) {
                }
                latch.countDown();
            });
        }

        latch.await();
        executorService.shutdown();

        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> issuedCouponRepository.count() == totalQuantity);

        //then
        assertEquals(totalQuantity, issuedCouponRepository.count());
    }
}