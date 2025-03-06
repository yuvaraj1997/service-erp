package com.yukadeeca.service_erp.common.constant;

import java.util.HashMap;
import java.util.Map;

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

    public static final Map<String, Integer> verificationValidityInSeconds = new HashMap<>();

    static {
        verificationValidityInSeconds.put(TYPE_EMAIL_VERIFICATION, 60 * 60 * 2);
        verificationValidityInSeconds.put(TYPE_PASSWORD_RESET, 60 * 60 * 2);
    }

}
