package com.firstcoupon.exception;


public class InvalidCouponCode extends CustomException {

    public InvalidCouponCode() {
        super(ErrorCode.INVALID_COUPON_CODE);
    }
}
