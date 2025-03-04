package com.firstcoupon.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CouponCreate {

    @NotBlank(message = "쿠폰 이름을 입력해 주세요.")
    private String couponName;

    @NotNull(message = "쿠폰 총 발급 수량을 입력해 주세요.")
    @Min(value = 1, message = "1개 이상 입력해 주세요.")
    private int totalQuantity;

    @NotNull(message = "쿠폰 만료일을 입력해 주세요.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate expirationDate;

    @NotNull(message = "쿠폰 발급 시작 시간을 입력해 주세요.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime issueStartTime;

    @NotNull(message = "쿠폰 발급 종료 시간을 입력해 주세요.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime issueEndTime;
}
