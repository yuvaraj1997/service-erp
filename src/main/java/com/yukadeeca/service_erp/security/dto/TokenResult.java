package com.yukadeeca.service_erp.security.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yukadeeca.service_erp.common.constant.JwtConstants;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResult {

    private final String refreshToken;
    private final String accessToken;
    private final Long expiresInSeconds;
    private final LocalDateTime expiration;
    private final LocalDateTime issuedAt;
    @JsonIgnore
    private final String jti;

    public TokenResult(String token, String jti, LocalDateTime issuedAt, LocalDateTime expiration, JwtConstants.TokenType tokenType) {
        if (tokenType == JwtConstants.TokenType.REFRESH_TOKEN) {
            this.refreshToken = token;
            this.accessToken = null;
        } else {
            this.refreshToken = null;
            this.accessToken = token;
        }
        this.issuedAt = issuedAt;
        this.expiration = expiration;
        this.expiresInSeconds = ChronoUnit.SECONDS.between(issuedAt, expiration);
        this.jti = jti;
    }
}
