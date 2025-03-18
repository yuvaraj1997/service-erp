package com.yukadeeca.service_erp.common.constant.emailTemplate;

import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;


@NoArgsConstructor
public class UserLoginOtpVerification implements EmailTemplateBuilder {

    String name;
    String otpCode;
    Integer otpValidityInMinutes;

    // Factory method to create a new instance
    public static UserLoginOtpVerification builder() {
        return new UserLoginOtpVerification();
    }

    public UserLoginOtpVerification name(String name) {
        this.name = name;
        return this;
    }

    public UserLoginOtpVerification otpCode(String otpCode) {
        this.otpCode = otpCode;
        return this;
    }

    public UserLoginOtpVerification otpValidityInMinutes(Integer otpValidityInMinutes) {
        this.otpValidityInMinutes = otpValidityInMinutes;
        return this;
    }

    @Override
    public String getSubject() {
        return "Multi-Factor Authentication (MFA) Code for Your Account";
    }

    @Override
    public String getTemplate() {
        return "verification/otp-verification.ftl";
    }

    @Override
    public Map<String, Object> build() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("otp_code", otpCode);
        payload.put("otp_validity_minutes", otpValidityInMinutes);
        return payload;
    }
}
