package com.firstcoupon.controller;

import com.firstcoupon.dto.CouponIssue;
import com.firstcoupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupon")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/issue")
    public void issueCoupon(@RequestBody CouponIssue request) {
        couponService.issueCouponWithKafka(request);
    }

    @PostMapping("/use")
    public void useCoupon(@RequestParam Long userId) {
        couponService.useCoupon(userId);
    }
}
