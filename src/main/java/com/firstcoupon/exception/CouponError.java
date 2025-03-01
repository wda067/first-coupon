package com.firstcoupon.exception;


public class CouponError extends CustomException {

    public CouponError() {
        super(ErrorCode.COUPON_ERROR);
    }
}
