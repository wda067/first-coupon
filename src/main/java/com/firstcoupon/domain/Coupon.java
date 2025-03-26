package com.firstcoupon.domain;

import com.firstcoupon.utils.CouponCodeGenerator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_coupon", columnNames = {"coupon_name", "expiration_date"})
        }
)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private String couponName;

    @OneToMany(mappedBy = "coupon", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private final List<IssuedCoupon> issuedCoupons = new ArrayList<>();

    private int totalQuantity;

    private int remainingQuantity;

    private LocalDate expirationDate;

    private LocalDateTime issueStartTime;

    private LocalDateTime issueEndTime;

    @Builder
    public Coupon(String code, String couponName, int totalQuantity, LocalDate expirationDate,
                  LocalDateTime issueStartTime, LocalDateTime issueEndTime) {
        this.code = code;
        this.couponName = couponName;
        this.totalQuantity = totalQuantity;
        this.remainingQuantity = totalQuantity;
        this.expirationDate = expirationDate;
        this.issueStartTime = issueStartTime;
        this.issueEndTime = issueEndTime;
    }

    public static Coupon create(String couponName, int totalQuantity, LocalDate expirationDate,
                                LocalDateTime issueStartTime, LocalDateTime issueEndTime) {
        String code = CouponCodeGenerator.generate();
        return Coupon.builder()
                .code(code)
                .couponName(couponName)
                .totalQuantity(totalQuantity)
                .expirationDate(expirationDate)
                .issueStartTime(issueStartTime)
                .issueEndTime(issueEndTime)
                .build();
    }

    public boolean isIssuable() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(issueStartTime) && now.isBefore(issueEndTime);
    }

    public long getDuration() {
        return Duration.between(issueStartTime, issueEndTime).getSeconds();
    }

    public void decrementQuantity() {
        if (remainingQuantity > 0) {
            remainingQuantity--;
        }
    }
}
