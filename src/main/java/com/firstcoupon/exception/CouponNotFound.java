package com.firstcoupon.exception;


public class CouponNotFound extends CustomException {

    public CouponNotFound() {
        super(ErrorCode.COUPON_NOT_FOUND);
    }
}
