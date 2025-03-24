package com.yukadeeca.service_erp.common.constant;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum SuccessCode {

    OTP_REQUESTED("OTP_REQUESTED_001", HttpStatus.OK.value(), "OTP successfully requested");

    private final String code;
    private final Integer status;
    private final String message;

}
