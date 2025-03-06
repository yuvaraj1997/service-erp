package com.yukadeeca.service_erp.user.service;


import com.yukadeeca.service_erp.common.constant.EmailConstants;
import com.yukadeeca.service_erp.common.constant.VerificationTokenConstants;
import com.yukadeeca.service_erp.common.exception.VerificationTokenException;
import com.yukadeeca.service_erp.common.service.email.IEmailService;
import com.yukadeeca.service_erp.user.entity.User;
import com.yukadeeca.service_erp.user.entity.VerificationToken;
import com.yukadeeca.service_erp.user.repository.VerificationTokenRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class VerificationTokenService {

    @Autowired
    VerificationTokenRepository verificationTokenRepository;

    @Autowired
    IEmailService emailService;

    @Value("${base.url.ui}")
    private String baseUrlUi;

    public void sendEmailVerificationEmail(User user) {
        String type = VerificationTokenConstants.TYPE_EMAIL_VERIFICATION;

        verificationTokenRepository.updateTokenStatus(user, type,
                                                            VerificationTokenConstants.STATUS_PENDING,
                                                            VerificationTokenConstants.STATUS_REVOKED);

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setUser(user);
        verificationToken.setType(type);

        Integer secondsToExpired = VerificationTokenConstants.verificationValidityInSeconds.get(type);

        if (null == secondsToExpired) {
            throw new RuntimeException(String.format("Unable to find seconds to expired for sending verification email, userId=%s , type=%s", user.getId(), type));
        }

        verificationToken.setExpiryDate(verificationToken.getCreatedAt().plusSeconds(secondsToExpired));

        verificationTokenRepository.save(verificationToken);

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", user.getFirstName());
        payload.put("verification_link", buildVerificationUrl(verificationToken.getToken()));

        emailService.sendHtmlEmail(user.getEmail(), EmailConstants.EMAIL_VERIFICATION_SUBJECT, payload, EmailConstants.EMAIL_VERIFICATION_TEMPLATE);
    }

    String buildVerificationUrl(String token) {
        return UriComponentsBuilder.fromUriString(baseUrlUi).path("/setPassword").queryParam("token", token).toUriString();
    }

    public VerificationToken verifyToken(String token, String type) {
        String errorMsg = "Invalid token or token already expired.";

        VerificationToken verificationToken = verificationTokenRepository.findByTokenAndType(token, type);

        if (null == verificationToken) {
            log.info("Token not found token={} , type={}", token, type);
            throw new VerificationTokenException(errorMsg);
        }

        if (!Objects.equals(verificationToken.getStatus(), VerificationTokenConstants.STATUS_PENDING)) {
            log.info("Token is not in [{}] , token={} , status={}", VerificationTokenConstants.STATUS_PENDING, token, verificationToken.getStatus());
            throw new VerificationTokenException(errorMsg);
        }

        if (LocalDateTime.now().isAfter(verificationToken.getExpiryDate())) {
            log.info("Token is expired , token={} , expiryDate={}", token, verificationToken.getExpiryDate().toString());
            verificationToken.setStatus(VerificationTokenConstants.STATUS_EXPIRED);
            throw new VerificationTokenException(errorMsg);
        }

        verificationToken.setStatus(VerificationTokenConstants.STATUS_USED);
        verificationToken.setUsedAt(LocalDateTime.now());
        verificationTokenRepository.save(verificationToken);

        log.info("Successfully verified the token, token={}", token);

        return verificationToken;
    }
}
