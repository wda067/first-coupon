package com.firstcoupon.exception;


public class CouponLockFailed extends CustomException {

    public CouponLockFailed() {
        super(ErrorCode.COUPON_LOCK_FAILED);
    }
}
