package com.firstcoupon.exception;


public class CouponAlreadyUsed extends CustomException {

    public CouponAlreadyUsed() {
        super(ErrorCode.COUPON_ALREADY_USED);
    }
}
