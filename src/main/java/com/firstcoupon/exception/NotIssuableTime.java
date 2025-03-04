package com.firstcoupon.exception;


public class NotIssuableTime extends CustomException {

    public NotIssuableTime() {
        super(ErrorCode.NOT_ISSUABLE_TIME);
    }
}
