package com.yukadeeca.service_erp.common.exception;

public class VerificationTokenException extends BaseAppException {

    public VerificationTokenException(String message) {
        super(message, "VERIFICATION_TOKEN_ERROR");
    }

}
