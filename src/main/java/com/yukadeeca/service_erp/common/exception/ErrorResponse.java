package com.yukadeeca.service_erp.common.exception;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {
    private String message;
    private String errorCode;
    private int status;
    private String timestamp;
    private String path;

    public ErrorResponse(String message, String errorCode, int status, String path) {
        this.message = message;
        this.errorCode = errorCode;
        this.status = status;
        this.timestamp = LocalDateTime.now().toString();
        this.path = path;
    }
}