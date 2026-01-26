package com.firstcoupon.repository;

import com.firstcoupon.domain.IssuedCoupon;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssuedCouponRepository extends JpaRepository<IssuedCoupon, Long> {

    Optional<IssuedCoupon> findByEmail(String email);

    long countByCouponId(Long couponId);
}
