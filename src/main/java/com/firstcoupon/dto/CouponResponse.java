package com.firstcoupon.dto;

import com.firstcoupon.domain.Coupon;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponResponse {

    private String code;

    private String couponName;

    private int totalQuantity;

    private LocalDate expirationDate;

    public CouponResponse(Coupon coupon) {
        this.code = coupon.getCode();
        this.couponName = coupon.getCouponName();
        this.totalQuantity = coupon.getTotalQuantity();
        this.expirationDate = coupon.getExpirationDate();
    }
}
