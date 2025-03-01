package com.firstcoupon.exception;


public class CouponAlreadyIssued extends CustomException {

    public CouponAlreadyIssued() {
        super(ErrorCode.COUPON_ALREADY_ISSUED);
    }
}
