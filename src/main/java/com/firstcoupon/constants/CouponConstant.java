package com.firstcoupon.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponConstant {

    MAX_COUPON_COUNT(1000);

    private final int value;
}
