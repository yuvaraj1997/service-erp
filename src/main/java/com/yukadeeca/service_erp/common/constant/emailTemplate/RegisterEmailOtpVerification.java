package com.yukadeeca.service_erp.common.constant.emailTemplate;

import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;


@NoArgsConstructor
public class RegisterEmailOtpVerification implements EmailTemplateBuilder {

    String name;
    String verificationLink;
    Integer otpValidityInMinutes;

    // Factory method to create a new instance
    public static RegisterEmailOtpVerification builder() {
        return new RegisterEmailOtpVerification();
    }

    public RegisterEmailOtpVerification name(String name) {
        this.name = name;
        return this;
    }

    public RegisterEmailOtpVerification verificationLink(String verificationLink) {
        this.verificationLink = verificationLink;
        return this;
    }

    public RegisterEmailOtpVerification otpValidityInMinutes(Integer otpValidityInMinutes) {
        this.otpValidityInMinutes = otpValidityInMinutes;
        return this;
    }

    @Override
    public String getSubject() {
        return "Email Verification";
    }

    @Override
    public String getTemplate() {
        return "verification/email-verification.ftl";
    }

    @Override
    public Map<String, Object> build() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("verification_link", verificationLink);
//        payload.put("otp_validity_minutes", otpValidityInMinutes);
        return payload;
    }
}
