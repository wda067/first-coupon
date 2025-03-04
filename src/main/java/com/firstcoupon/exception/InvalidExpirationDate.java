package com.firstcoupon.exception;


public class InvalidExpirationDate extends CustomException {

    public InvalidExpirationDate() {
        super(ErrorCode.INVALID_EXPIRATION_DATE);
    }
}
