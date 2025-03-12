package com.firstcoupon.exception;


import static com.firstcoupon.exception.ErrorCode.EMAIL_SEND_FAILURE;

public class EmailSendFailure extends CustomException {

    public EmailSendFailure() {
        super(EMAIL_SEND_FAILURE);
    }
}
