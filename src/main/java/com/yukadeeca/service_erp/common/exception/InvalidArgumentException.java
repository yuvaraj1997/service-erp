package com.yukadeeca.service_erp.common.exception;

public class InvalidArgumentException extends BaseAppException {

    public InvalidArgumentException(String message) {
        super(message, "INVALID_ARGUMENT");
    }

}
