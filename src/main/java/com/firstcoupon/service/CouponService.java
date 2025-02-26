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

    private final CouponRepository couponRepository;

    @Transactional
    public void issueCoupon(Long userId) {
        long count = couponRepository.count();

        if (count > MAX_COUPON) {
            throw new IllegalStateException("쿠폰이 모두 소진되었습니다.");
        }

        couponRepository.save(new Coupon(userId));
    }
}
