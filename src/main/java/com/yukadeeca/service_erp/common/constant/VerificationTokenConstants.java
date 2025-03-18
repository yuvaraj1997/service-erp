package com.yukadeeca.service_erp.common.constant;

import com.yukadeeca.service_erp.common.constant.emailTemplate.EmailTemplateBuilder;
import com.yukadeeca.service_erp.common.constant.emailTemplate.RegisterEmailOtpVerification;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Supplier;

public final class VerificationTokenConstants {
    private VerificationTokenConstants() {
        // Prevent instantiation
    }

    public static final String TYPE_EMAIL_VERIFICATION = "EMAIL_VERIFICATION";
    public static final String TYPE_PASSWORD_RESET = "PASSWORD_RESET";

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_USED = "USED";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_REVOKED = "REVOKED";

    @AllArgsConstructor
    @Getter
    public enum Type {
        REGISTER_EMAIL_VERIFICATION(TYPE_EMAIL_VERIFICATION, 60 * 2, RegisterEmailOtpVerification::builder);

        final String type;
        final Integer otpValidityMinutes;
        final Supplier<EmailTemplateBuilder> emailTemplate;

        public EmailTemplateBuilder getBuilder() {
            return emailTemplate.get();
        }
    }

}
