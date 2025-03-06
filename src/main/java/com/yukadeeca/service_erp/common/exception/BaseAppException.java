package com.yukadeeca.service_erp.common.exception;

import lombok.Getter;

@Getter
public abstract class BaseAppException extends RuntimeException {
    private final String errorCode;

    public BaseAppException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

}