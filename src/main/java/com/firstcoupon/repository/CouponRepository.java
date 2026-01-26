package com.firstcoupon.repository;

import com.firstcoupon.domain.Coupon;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    boolean existsByCouponNameAndExpirationDate(String couponName, LocalDate expirationDate);

    Optional<Coupon> findByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.code = :code")
    Optional<Coupon> findByCodeForUpdate(@Param("code") String code);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Coupon c SET c.remainingQuantity = c.remainingQuantity - 1 "
            + "WHERE c.id = :couponId AND c.remainingQuantity > 0")
    void decrementQuantity(@Param("couponId") Long couponId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Coupon c set c.remainingQuantity = c.totalQuantity")
    int resetRemainingToTotal();
}
