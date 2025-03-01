package com.firstcoupon.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponMessage {

    COUPON_ALREADY_ISSUED("이미 쿠폰을 발급받은 회원입니다."),
    COUPON_SOLD_OUT("쿠폰이 모두 소진되었습니다."),
    COUPON_LOCK_FAILED("쿠폰 발급이 지연되고 있습니다. 잠시 후 다시 시도해주세요."),
    COUPON_ERROR("쿠폰 발급 과정에서 오류가 발생했습니다.");

    private final String message;
}
