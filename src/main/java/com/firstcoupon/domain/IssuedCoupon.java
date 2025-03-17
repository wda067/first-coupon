package com.firstcoupon.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssuedCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @CreatedDate
    private LocalDateTime issuedAt;

    private LocalDateTime usedAt;

    @Enumerated(EnumType.STRING)
    private CouponStatus status;

    @Builder
    public IssuedCoupon(String email, Coupon coupon, LocalDateTime usedAt, CouponStatus status) {
        this.email = email;
        this.coupon = coupon;
        this.usedAt = usedAt;
        this.status = status;
    }

    public static IssuedCoupon issue(String email, Coupon coupon) {
        return IssuedCoupon.builder()
                .email(email)
                .coupon(coupon)
                .status(CouponStatus.ISSUED)
                .build();
    }

    public void expire() {
        this.status = CouponStatus.EXPIRED;
    }

    public boolean isExpired(LocalDate expirationDate) {
        return LocalDate.now().isAfter(expirationDate);
    }

    public void use() {
        this.status = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
    }

    public boolean isUsed() {
        return this.status == CouponStatus.USED;
    }
}
