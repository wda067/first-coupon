package com.firstcoupon.repository;

import com.firstcoupon.domain.Coupon;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    boolean existsByCouponNameAndExpirationDate(String couponName, LocalDate expirationDate);

    Optional<Coupon> findByCode(String code);
}
