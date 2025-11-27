package com.firstcoupon.service;

import com.firstcoupon.domain.Coupon;
import com.firstcoupon.domain.CouponIssuedEvent;
import com.firstcoupon.domain.IssuedCoupon;
import com.firstcoupon.exception.CouponNotFound;
import com.firstcoupon.repository.CouponRepository;
import com.firstcoupon.repository.IssuedCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponIssueService {

    private final CouponRepository couponRepository;
    private final IssuedCouponRepository issuedCouponRepository;

    @Transactional
    public void handleCouponIssued(CouponIssuedEvent event) {
        Long couponId = event.getCouponId();
        couponRepository.decrementQuantity(couponId);  //JPQL 쿠폰 재고 감소
        Coupon coupon = couponRepository.findById(couponId)  //쿠폰 조회
                .orElseThrow(CouponNotFound::new);

        //쿠폰 발급
        IssuedCoupon issuedCoupon = IssuedCoupon.issue(event.getEmail(), coupon);
        issuedCouponRepository.save(issuedCoupon);
    }
}
