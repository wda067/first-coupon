package com.firstcoupon.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firstcoupon.domain.Coupon;
import com.firstcoupon.domain.CouponStatus;
import com.firstcoupon.domain.IssuedCoupon;
import com.firstcoupon.dto.CouponIssue;
import com.firstcoupon.repository.CouponRepository;
import com.firstcoupon.repository.IssuedCouponRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private IssuedCouponRepository issuedCouponRepository;

    private String code;

    @BeforeAll
    void setUp() {
        Coupon coupon = Coupon.create("테스트 코드", 100, LocalDate.now().plusDays(7),
                LocalDateTime.now(), LocalDateTime.now().plusDays(7));
        couponRepository.save(coupon);
        code = coupon.getCode();
    }

    @BeforeEach
    void clean() {
        issuedCouponRepository.deleteAll();
    }

    @Test
    void 쿠폰_발급_요청을_한다() throws Exception {
        //given
        CouponIssue request = new CouponIssue(code, "test@test.com");
        String json = objectMapper.writeValueAsString(request);

        //when
        mockMvc.perform(post("/coupon/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andDo(print());

        Thread.sleep(1000);

        //then
        IssuedCoupon issuedCoupon = issuedCouponRepository.findAll().get(0);
        assertEquals("test@test.com", issuedCoupon.getEmail());

        assertEquals(CouponStatus.ISSUED, issuedCoupon.getStatus());
        assertNull(issuedCoupon.getUsedAt());
    }

    @Test
    @Transactional
    void 발급받은_쿠폰을_사용한다() throws Exception{
        //given
        Coupon coupon = couponRepository.findAll().get(0);
        String email = "test@test.com";
        IssuedCoupon issuedCoupon = IssuedCoupon.issue(email, coupon);
        issuedCouponRepository.save(issuedCoupon);

        //when
        mockMvc.perform(post("/coupon/use")
                        .param("email", email))
                .andExpect(status().isOk())
                .andDo(print());

        //then
        assertEquals(CouponStatus.USED, issuedCoupon.getStatus());
    }

    @Test
    @Transactional
    void 사용한_쿠폰은_재사용할_수_없다() throws Exception{
        //given
        Coupon coupon = couponRepository.findAll().get(0);
        String email = "test@test.com";
        IssuedCoupon issuedCoupon = IssuedCoupon.issue(email, coupon);
        issuedCouponRepository.save(issuedCoupon);

        mockMvc.perform(post("/coupon/use")
                        .param("email", email))
                .andExpect(status().isOk());

        //expected
        mockMvc.perform(post("/coupon/use")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("이미 사용한 쿠폰입니다."))
                .andDo(print());
    }
}
