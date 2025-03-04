package com.firstcoupon.utils;

import java.security.SecureRandom;

public class CouponCodeGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate() {
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 12; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }

        return formatWithHyphens(code.toString());
    }

    private static String formatWithHyphens(String code) {
        return code.replaceAll("(.{4})", "$1-").substring(0, 14);  //4자리마다 '-' 추가
    }
}
