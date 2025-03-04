package com.firstcoupon.controller;

import com.firstcoupon.dto.CouponCreate;
import com.firstcoupon.dto.CouponResponse;
import com.firstcoupon.service.AdminService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/admin/coupon")
    public void createCoupon(@RequestBody CouponCreate request) {
        adminService.createCoupon(request);
    }

    @GetMapping("/admin/coupons")
    public List<CouponResponse> getCoupons() {
        return adminService.getCoupons();
    }
}
