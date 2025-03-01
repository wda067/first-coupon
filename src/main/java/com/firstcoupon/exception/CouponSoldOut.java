package com.firstcoupon.exception;


public class CouponSoldOut extends CustomException {

    public CouponSoldOut() {
        super(ErrorCode.COUPON_SOLD_OUT);
    }
}
