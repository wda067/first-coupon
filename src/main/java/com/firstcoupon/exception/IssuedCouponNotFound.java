package com.firstcoupon.exception;


public class IssuedCouponNotFound extends CustomException {

    public IssuedCouponNotFound() {
        super(ErrorCode.ISSUED_COUPON_NOT_FOUND);
    }
}
