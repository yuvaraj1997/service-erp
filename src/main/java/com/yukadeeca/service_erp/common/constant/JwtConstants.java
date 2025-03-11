package com.yukadeeca.service_erp.common.constant;

public final class JwtConstants {
    private JwtConstants() {
        // Prevent instantiation
    }

    public static final String ISSUER = "erp";
    public static final String CLAIMS_ROLES = "roles";
    public static final String CLAIMS_UUID = "roles";

    public enum TokenType {
        REFRESH_TOKEN("REFRESH_TOKEN", 60 * 60 * 2), //2 hours
        ACCESS_TOKEN("ACCESS_TOKEN", 60 * 15); //15 minutes

        final String type;
        final Integer expirationTimeInSeconds;

        TokenType(String type, Integer expirationTimeInSeconds) {
            this.type = type;
            this.expirationTimeInSeconds = expirationTimeInSeconds;
        }

        public String getType() {
            return type;
        }

        public Integer getExpirationTimeInSeconds() {
            return expirationTimeInSeconds;
        }
    }

}
