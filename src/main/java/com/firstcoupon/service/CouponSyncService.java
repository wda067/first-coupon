package com.firstcoupon.service;

import com.firstcoupon.dto.CouponIssue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponSyncService {

    private final CouponService couponService;

    public synchronized void issueCouponWithSynchronized(CouponIssue request) {
        couponService.issueCouponWithSynchronized(request);
    }
}
