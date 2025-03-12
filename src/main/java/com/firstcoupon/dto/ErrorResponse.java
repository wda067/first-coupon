package com.firstcoupon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.firstcoupon.exception.ErrorCode;
import java.util.List;
import lombok.Getter;
import org.springframework.validation.FieldError;

@Getter
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)  //빈 값은 제외
public class ErrorResponse {

    private final String code;
    private final String message;
    private final ObjectNode body = new ObjectMapper().createObjectNode();

    public ErrorResponse(ErrorCode errorCode, List<FieldError> fieldErrors) {
        this.code = errorCode.toString();
        this.message = errorCode.getMessage();
        fieldErrors.forEach(error -> this.body.put(error.getField(), error.getDefaultMessage()));
    }

    public ErrorResponse(ErrorCode errorCode) {
        this.code = errorCode.toString();
        this.message = errorCode.getMessage();
    }

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
