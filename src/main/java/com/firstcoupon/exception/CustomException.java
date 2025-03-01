package com.firstcoupon.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

@Getter
public abstract class CustomException extends RuntimeException {

    private final ErrorCode errorCode;
    private final ObjectNode validation = new ObjectMapper().createObjectNode();

    public CustomException(ErrorCode errorCode) {
        super();
        this.errorCode = errorCode;
    }
}