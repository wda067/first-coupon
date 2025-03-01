package com.firstcoupon.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    COUPON_ALREADY_ISSUED("409", "이미 쿠폰을 발급받았습니다."),
    COUPON_SOLD_OUT("410", "쿠폰이 모두 소진되었습니다."),
    COUPON_LOCK_FAILED("503", "쿠폰 발급이 지연되고 있습니다. 잠시 후 다시 시도해주세요."),
    COUPON_ERROR("500", "쿠폰 발급 과정에서 오류가 발생했습니다.");

    private final String code;
    private final String message;
}
