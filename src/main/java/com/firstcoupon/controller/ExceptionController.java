package com.firstcoupon.controller;

import com.firstcoupon.dto.ErrorResponse;
import com.firstcoupon.exception.CustomException;
import com.firstcoupon.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionController {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse exceptionHandler(MethodArgumentNotValidException exception) {
        return new ErrorResponse(ErrorCode.BAD_REQUEST, exception.getFieldErrors());
    }

    @ExceptionHandler(CustomException.class)
    public ErrorResponse exceptionHandler(CustomException e) {
        return new ErrorResponse(e.getErrorCode());
    }
}
