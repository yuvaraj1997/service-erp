package com.yukadeeca.service_erp.security.util;

import com.yukadeeca.service_erp.common.constant.JwtConstants;
import com.yukadeeca.service_erp.security.dto.AuthorizationToken;
import com.yukadeeca.service_erp.security.dto.TokenResult;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.yukadeeca.service_erp.common.util.DateUtil.nowDate;
import static com.yukadeeca.service_erp.common.util.DateUtil.toDate;

@Slf4j
@Component
public class JwtUtil {

    private static final String KEY_ALGORITHM = "RSA";

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public JwtUtil(@Value("${private.key}") String privateKeyBase64, @Value("${public.key}") String publicKeyBase64) throws NoSuchAlgorithmException, InvalidKeySpecException, GeneralSecurityException {
        privateKey = loadPrivateKey(privateKeyBase64);
        publicKey = loadPublicKey(publicKeyBase64);
    }

    private PrivateKey loadPrivateKey(String privateKeyBase64) throws GeneralSecurityException {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(privateKeyBase64);
            String cleanedKey = new String(decodedKey)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(cleanedKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance(KEY_ALGORITHM).generatePrivate(keySpec);
        } catch (IllegalArgumentException e) {
            throw new InvalidKeySpecException("Invalid Base64 encoding for private key", e);
        }
    }

    private PublicKey loadPublicKey(String publicKeyBase64) throws GeneralSecurityException {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(publicKeyBase64);
            String cleanedKey = new String(decodedKey)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(cleanedKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(keySpec);
        } catch (IllegalArgumentException e) {
            throw new InvalidKeySpecException("Invalid Base64 encoding for public key", e);
        }
    }

    public TokenResult generateRefreshToken(String subject, Map<String, Object> claims) {
        return generateToken(subject, claims, JwtConstants.TokenType.REFRESH_TOKEN, null);
    }

    public TokenResult generateAccessToken(String subject, Map<String, Object> claims, String sessionId) {
        return generateToken(subject, claims, JwtConstants.TokenType.ACCESS_TOKEN, sessionId);
    }

    private TokenResult generateToken(String subject, Map<String, Object> claims, JwtConstants.TokenType tokenType, String sessionId) {
        LocalDateTime now = nowDate();
        LocalDateTime expiration = now.plusSeconds(tokenType.getExpirationTimeInSeconds());
        String jti = sessionId != null ? sessionId : UUID.randomUUID().toString(); // Generate unique jti

        if (null == claims) {
            claims = new HashMap<>();
        }

        String token = Jwts.builder()
                .header()
                .type(tokenType.getType())
                .and()
                .issuer(JwtConstants.ISSUER)
                .issuedAt(toDate(now))
                .expiration(toDate(expiration))
                .subject(subject)
                .id(jti)
                .claims(claims)
                .signWith(privateKey)
                .compact();


        return new TokenResult(token, jti, now, expiration, tokenType);
    }

    public Jws<Claims> getJws(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(publicKey)
                .clockSkewSeconds(Long.MAX_VALUE / 1000)
                .build()
                .parseSignedClaims(token);
    }

    /**
     * Parses a JWT and returns its claims.
     *
     * @param token The JWT string to parse
     * @return The Claims object containing all claims from the JWT
     * @throws JwtException If the token is invalid, expired, or malformed
     */
    public Claims getClaims(String token) throws JwtException {
        return getJws(token).getPayload();
    }

    public JwsHeader getHeader(String token) throws JwtException {
        return getJws(token).getHeader();
    }

    // Extract username from token
    public AuthorizationToken parseToken(String token) {
        Jws<Claims> jws = getJws(token);
        return new AuthorizationToken(jws.getHeader(), jws.getPayload());
    }

    public String extractTokenType(String token) {
        return getHeader(token).getType();
    }


    /**
     * Checks if the token is valid (not expired and properly signed).
     *
     * @param token The JWT string
     * @return True if the token is valid, false otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = getClaims(token);
            return toDate(nowDate()).before(claims.getExpiration());
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("{}: Invalid token, errorMessage={}", ex.getClass().getSimpleName(), ex.getMessage());
            return false; // Invalid signature, expired, or malformed token
        }
    }
}
