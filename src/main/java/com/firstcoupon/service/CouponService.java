package com.firstcoupon.service;

import com.firstcoupon.domain.Coupon;
import com.firstcoupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private static final int MAX_COUPON = 100;
    private static final String COUPON_ALREADY_ISSUED = "이미 쿠폰을 발급받은 회원입니다.";
    private static final String COUPON_SOLD_OUT = "쿠폰이 모두 소진되었습니다.";

    private final CouponRepository couponRepository;

    @Transactional
    public void issueCoupon(Long userId) {
        boolean isAlreadyIssued = couponRepository.findByUserId(userId).isPresent();
        if (isAlreadyIssued) {
            throw new IllegalStateException(COUPON_ALREADY_ISSUED);
        }

        long count = couponRepository.count();
        if (count >= MAX_COUPON) {
            throw new IllegalStateException(COUPON_SOLD_OUT);
        }

        couponRepository.save(new Coupon(userId));
    }

    @Transactional
    public synchronized void issueCouponWithSynchronized(Long userId) {
        boolean isAlreadyIssued = couponRepository.findByUserId(userId).isPresent();
        if (isAlreadyIssued) {
            throw new IllegalStateException(COUPON_ALREADY_ISSUED);
        }

        long count = couponRepository.count();
        if (count >= MAX_COUPON) {
            throw new IllegalStateException(COUPON_SOLD_OUT);
        }

        couponRepository.save(new Coupon(userId));
    }
}
