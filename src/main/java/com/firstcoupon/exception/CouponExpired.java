package com.firstcoupon.exception;


public class CouponExpired extends CustomException {

    public CouponExpired() {
        super(ErrorCode.COUPON_EXPIRED);
    }
}
