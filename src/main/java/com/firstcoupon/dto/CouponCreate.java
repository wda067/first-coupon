package com.firstcoupon.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CouponCreate {

    private String couponName;
    private int totalQuantity;
    private String expirationDate;
    private String issueStartTime;
    private String issueEndTime;
}
