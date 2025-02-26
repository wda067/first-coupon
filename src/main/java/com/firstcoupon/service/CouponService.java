package com.firstcoupon.service;

import com.firstcoupon.domain.Coupon;
import com.firstcoupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private static final int MAX_COUPON = 100;
    private static final String COUPON_EXHAUSTED_MESSAGE = "쿠폰이 모두 소진되었습니다.";

    private final CouponRepository couponRepository;

    @Transactional
    public void issueCoupon(Long userId) {
        long count = couponRepository.count();

        if (count >= MAX_COUPON) {
            throw new IllegalStateException(COUPON_EXHAUSTED_MESSAGE);
        }

        couponRepository.save(new Coupon(userId));
    }

    @Transactional
    public synchronized void issueCouponWithSynchronized(Long userId) {
        long count = couponRepository.count();

        if (count >= MAX_COUPON) {
            throw new IllegalStateException(COUPON_EXHAUSTED_MESSAGE);
        }

        couponRepository.save(new Coupon(userId));
    }
}
