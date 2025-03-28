package com.firstcoupon.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firstcoupon.domain.Coupon;
import com.firstcoupon.dto.CouponCreate;
import com.firstcoupon.repository.CouponRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CouponRepository couponRepository;

    @BeforeEach
    void setUp() {
        couponRepository.deleteAll();
    }

    @Test
    void 쿠폰을_생성한다() throws Exception {
        //given
        CouponCreate request = new CouponCreate(
                "테스트 쿠폰",
                1000, LocalDate.now().plusDays(7),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7));
        String json = objectMapper.writeValueAsString(request);

        //when
        mockMvc.perform(post("/api/admin/coupon")
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andDo(print());

        //then
        Coupon coupon = couponRepository.findAll().get(0);

        assertEquals("테스트 쿠폰", coupon.getCouponName());
        assertEquals(1000, coupon.getTotalQuantity());
        assertEquals(1L, couponRepository.count());
    }

    @Test
    void 쿠폰_목록을_조회한다() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> coupons = IntStream.range(1, 11)
                .mapToObj(i -> Coupon.create("테스트 쿠폰" + i,
                        100,
                        now.toLocalDate().plusDays(7),
                        now,
                        now.plusDays(7))
                )
                .toList();
        couponRepository.saveAll(coupons);

        //expected
        mockMvc.perform(get("/api/admin/coupons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0].couponName").value("테스트 쿠폰1"))
                .andExpect(jsonPath("$[0].totalQuantity").value(100))
                .andDo(print());

    }
}