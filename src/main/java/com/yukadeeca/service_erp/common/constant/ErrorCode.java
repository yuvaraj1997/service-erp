package com.yukadeeca.service_erp.common.constant;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    //General Error
    INTERNAL_SERVER_ERROR("SYS_001", HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"),
    INVALID_REQUEST("SYS_002", HttpStatus.BAD_REQUEST.value(), "Invalid request"),
    UNAUTHORIZED("SYS_003", HttpStatus.UNAUTHORIZED.value(), "Unauthorized access"),
    FORBIDDEN("SYS_004", HttpStatus.FORBIDDEN.value(), "Access Denied"),

    // Authentication errors
    INVALID_CREDENTIALS("AUTH_001", HttpStatus.BAD_REQUEST.value(), "Invalid username or password"),
    OTP_COOL_DOWN("AUTH_002", HttpStatus.BAD_REQUEST.value(), "OTP Request cool down window. Please wait 15 minutes."),
    OTP_RESEND_TOO_FREQUENT("AUTH_003", HttpStatus.BAD_REQUEST.value(), "OTP Request can be done only after 60 seconds."),
    OTP_REQUEST_MAX("AUTH_004", HttpStatus.BAD_REQUEST.value(), "OTP Request max reached."),
    OTP_INVALID("AUTH_005", HttpStatus.BAD_REQUEST.value(), "Invalid OTP."),

    // User Registration Error
    VERIFICATION_TOKEN_ERROR("USR_REGISTER_001", HttpStatus.BAD_REQUEST.value(), "Verification Token Error"),

    // User errors
    USER_NOT_FOUND("USR_001", HttpStatus.BAD_REQUEST.value(), "User not found"),
    USER_ALREADY_EXISTS("USR_002", HttpStatus.BAD_REQUEST.value(), "User already exists");

    private final String code;
    private final Integer status;
    private final String message;

}
