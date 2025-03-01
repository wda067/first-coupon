package com.firstcoupon.controller;

import com.firstcoupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/coupon/issue/{userId}")
    public void issueCoupon(@PathVariable Long userId) {
        couponService.issueCouponWithKafka(userId);
    }
}
