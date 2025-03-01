package com.firstcoupon.controller;

import com.firstcoupon.dto.ErrorResponse;
import com.firstcoupon.exception.CustomException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(CustomException.class)
    public ErrorResponse exceptionHandler(CustomException e) {
        return new ErrorResponse(e.getErrorCode());
    }
}
