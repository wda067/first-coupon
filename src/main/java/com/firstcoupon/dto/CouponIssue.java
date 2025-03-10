package com.firstcoupon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CouponIssue {

    @NotBlank(message = "쿠폰 코드를 입력해 주세요.")
    private String code;

    @NotNull
    private String email;
}
