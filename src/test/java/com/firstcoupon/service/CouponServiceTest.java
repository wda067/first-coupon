package com.firstcoupon.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.firstcoupon.repository.CouponRepository;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CouponServiceTest {

    private static final int MAX_COUPON = 100; // CouponService의 MAX_COUPON과 동일하게 설정
    private static final int THREAD_COUNT = 150; // 최대 쿠폰보다 많은 스레드로 경쟁 유발

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @BeforeEach
    void setUp() {
        couponRepository.deleteAll();
    }

    @Test
    void 쿠폰을_1개_발행한다() {
        //given
        Long userId = 1L;

        //when
        couponService.issueCoupon(userId);

        //then
        assertEquals(1L, couponRepository.count());
    }

    @Test
    void 경쟁_조건으로_쿠폰이_초과_발급된다() throws InterruptedException {
        //given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        //when
        for (int i = 1; i <= THREAD_COUNT; i++) {
            long userId = i;
            executorService.execute(() -> {
                try {
                    couponService.issueCoupon(userId);
                } catch (Exception ignored) {
                }
                latch.countDown();
            });
        }

        latch.await();
        executorService.shutdown();

        //then
        long issuedCoupons = couponRepository.count();
        assertTrue(issuedCoupons > MAX_COUPON,
                "경쟁 조건으로 인해 MAX_COUPON보다 많은 쿠폰이 발급되어야 함");
    }

    @Test
    void synchronized를_사용하여_동시에_100개의_쿠폰까지만_발급된다() throws InterruptedException {
        //given
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        //when
        for (int i = 1; i <= THREAD_COUNT; i++) {
            Long userId = (long) i;
            executorService.execute(() -> {
                try {
                    couponService.issueCouponWithSynchronized(userId);
                } catch (Exception ignored) {
                }
                latch.countDown();
            });
        }

        latch.await();
        executorService.shutdown();

        //then
        long issuedCoupons = couponRepository.count();
        assertEquals(MAX_COUPON, issuedCoupons);
    }
}