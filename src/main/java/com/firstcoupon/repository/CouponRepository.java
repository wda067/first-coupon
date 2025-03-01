package com.firstcoupon.repository;

import com.firstcoupon.domain.Coupon;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByUserId(Long userId);
}
