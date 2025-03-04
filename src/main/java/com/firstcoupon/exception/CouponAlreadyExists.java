package com.firstcoupon.exception;


public class CouponAlreadyExists extends CustomException {

    public CouponAlreadyExists() {
        super(ErrorCode.COUPON_ALREADY_EXISTS);
    }
}
