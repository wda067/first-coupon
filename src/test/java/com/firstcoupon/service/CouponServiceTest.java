package com.firstcoupon.service;

import static org.junit.jupiter.api.Assertions.*;

import com.firstcoupon.repository.CouponRepository;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CouponServiceTest {

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
    void 동시성_이슈로_100개_미만의_쿠폰이_발급된다() {
        //given
        int threadCount = 100;
        ExecutorService executorService = Executors.newCachedThreadPool();  //동적으로 스레드 수 조절

        //when
        for (int i = 1; i <= threadCount; i++) {  //동시에 100개의 쿠폰 발급
            Long userId = (long) i;
            executorService.execute(() -> {
                try {
                    couponService.issueCoupon(userId);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        executorService.shutdown();

        //then
        assertNotEquals(threadCount, couponRepository.count());
    }
}