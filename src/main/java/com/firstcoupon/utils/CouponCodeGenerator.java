package com.firstcoupon.utils;

import java.time.LocalDate;
import java.util.Random;

public class CouponCodeGenerator {

    public static String generate() {
        String date = LocalDate.now().toString().replace("-", "");
        int random = new Random().nextInt(9999);
        return "EVENT" + date + String.format("%04d", random);
    }
}
