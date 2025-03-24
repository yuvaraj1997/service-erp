package com.yukadeeca.service_erp.common.constant;

import com.yukadeeca.service_erp.common.constant.emailTemplate.EmailTemplateBuilder;
import com.yukadeeca.service_erp.common.constant.emailTemplate.UserLoginOtpVerification;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Supplier;

public final class UserOtpVerificationConstants {
    private UserOtpVerificationConstants() {
        // Prevent instantiation
    }

    public static final String TYPE_LOGIN = "LOGIN";

    @AllArgsConstructor
    @Getter
    public enum Type {
        LOGIN("LOGIN", 15, 60, 3, 5, UserLoginOtpVerification::builder);

        final String type;
        final Integer cooldownPeriodMinutes;
        final Integer resendIntervalSeconds;
        final Integer maxRequest;
        final Integer otpValidityMinutes;
        final Supplier<EmailTemplateBuilder> emailTemplate;

        public EmailTemplateBuilder getBuilder() {
            return emailTemplate.get();
        }
    }
}
