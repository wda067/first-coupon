package com.firstcoupon.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    COUPON_ALREADY_ISSUED("409", "이미 쿠폰을 발급받았습니다."),
    COUPON_ALREADY_USED("400", "이미 사용한 쿠폰입니다."),
    COUPON_ALREADY_EXISTS("409", "이미 존재하는 쿠폰입니다."),
    INVALID_EXPIRATION_DATE("400", "유효하지 않은 만료일입니다."),
    INVALID_COUPON_CODE("400", "유효하지 않은 쿠폰 코드입니다."),
    COUPON_SOLD_OUT("410", "쿠폰이 모두 소진되었습니다."),
    COUPON_LOCK_FAILED("503", "쿠폰 발급이 지연되고 있습니다. 잠시 후 다시 시도해주세요."),
    NOT_ISSUABLE_TIME("400", "현재는 쿠폰 발급이 불가능한 시간입니다."),
    COUPON_EXPIRED("400", "쿠폰이 만료되었습니다."),
    ISSUED_COUPON_NOT_FOUND("404", "발급 받은 쿠폰이 존재하지 않습니다."),
    COUPON_NOT_FOUND("404", "존재하지 않는 쿠폰입니다."),
    EMAIL_SEND_FAILURE("500", "이메일 전송에 실패했습니다."),
    COUPON_ERROR("500", "쿠폰 발급 과정에서 오류가 발생했습니다.");

    private final String code;
    private final String message;
}
