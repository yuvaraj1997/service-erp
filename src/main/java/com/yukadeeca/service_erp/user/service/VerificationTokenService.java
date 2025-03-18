package com.yukadeeca.service_erp.user.service;


import com.yukadeeca.service_erp.common.constant.VerificationTokenConstants;
import com.yukadeeca.service_erp.common.constant.emailTemplate.RegisterEmailOtpVerification;
import com.yukadeeca.service_erp.common.exception.ApplicationException;
import com.yukadeeca.service_erp.common.service.email.IEmailService;
import com.yukadeeca.service_erp.user.entity.User;
import com.yukadeeca.service_erp.user.entity.VerificationToken;
import com.yukadeeca.service_erp.user.repository.VerificationTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.yukadeeca.service_erp.common.constant.ErrorCode.VERIFICATION_TOKEN_ERROR;

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
        VerificationTokenConstants.Type type = VerificationTokenConstants.Type.REGISTER_EMAIL_VERIFICATION;

        verificationTokenRepository.updateTokenStatus(user, type.getType(),
                VerificationTokenConstants.STATUS_PENDING,
                VerificationTokenConstants.STATUS_REVOKED);

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setUser(user);
        verificationToken.setType(type.getType());

        verificationToken.setExpiryDate(verificationToken.getCreatedAt().plusMinutes(type.getOtpValidityMinutes()));

        verificationTokenRepository.save(verificationToken);

        RegisterEmailOtpVerification builder = (RegisterEmailOtpVerification) type.getBuilder();

        emailService.sendHtmlEmail(
                user.getEmail(),
                builder.getSubject(),
                builder.name(user.getFirstName())
                       .verificationLink(buildVerificationUrl(verificationToken.getToken()))
                       .build(),
                builder.getTemplate()
        );
    }

    String buildVerificationUrl(String token) {
        return UriComponentsBuilder.fromUriString(baseUrlUi).path("/setPassword").queryParam("token", token).toUriString();
    }

    public VerificationToken verifyToken(String token, String type) {
        String errorMsg = "Invalid token or token already expired.";

        VerificationToken verificationToken = verificationTokenRepository.findByTokenAndType(token, type);

        if (null == verificationToken) {
            log.info("Token not found token={} , type={}", token, type);
            throw new ApplicationException(VERIFICATION_TOKEN_ERROR, errorMsg);
        }

        if (!Objects.equals(verificationToken.getStatus(), VerificationTokenConstants.STATUS_PENDING)) {
            log.info("Token is not in [{}] , token={} , status={}", VerificationTokenConstants.STATUS_PENDING, token, verificationToken.getStatus());
            throw new ApplicationException(VERIFICATION_TOKEN_ERROR, errorMsg);
        }

        if (LocalDateTime.now().isAfter(verificationToken.getExpiryDate())) {
            log.info("Token is expired , token={} , expiryDate={}", token, verificationToken.getExpiryDate().toString());
            verificationToken.setStatus(VerificationTokenConstants.STATUS_EXPIRED);
            throw new ApplicationException(VERIFICATION_TOKEN_ERROR, errorMsg);
        }

        verificationToken.setStatus(VerificationTokenConstants.STATUS_USED);
        verificationToken.setUsedAt(LocalDateTime.now());
        verificationTokenRepository.save(verificationToken);

        log.info("Successfully verified the token, token={}", token);

        return verificationToken;
    }
}
