package com.firstcoupon.dto;

import com.firstcoupon.domain.Coupon;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponResponse {

    private String code;

    private String couponName;

    private int totalQuantity;

    private int remainingQuantity;

    private LocalDate expirationDate;

    private LocalDateTime issueStartTime;

    private LocalDateTime issueEndTime;

    public CouponResponse(Coupon coupon) {
        this.code = coupon.getCode();
        this.couponName = coupon.getCouponName();
        this.totalQuantity = coupon.getTotalQuantity();
        this.remainingQuantity = coupon.getRemainingQuantity();
        this.expirationDate = coupon.getExpirationDate();
        this.issueStartTime = coupon.getIssueStartTime();
        this.issueEndTime = coupon.getIssueEndTime();
    }
}
