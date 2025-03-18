package com.yukadeeca.service_erp.user.service;


import com.yukadeeca.service_erp.common.constant.UserOtpVerificationConstants;
import com.yukadeeca.service_erp.common.constant.emailTemplate.UserLoginOtpVerification;
import com.yukadeeca.service_erp.common.exception.ApplicationException;
import com.yukadeeca.service_erp.common.service.email.IEmailService;
import com.yukadeeca.service_erp.user.dto.OtpRequestResult;
import com.yukadeeca.service_erp.user.entity.User;
import com.yukadeeca.service_erp.user.entity.UserOtpVerification;
import com.yukadeeca.service_erp.user.repository.UserOtpVerificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static com.yukadeeca.service_erp.common.constant.ErrorCode.*;
import static com.yukadeeca.service_erp.common.util.DateUtil.nowDate;

@Slf4j
@Service
public class UserOtpVerificationService {

    @Autowired
    UserOtpVerificationRepository userOtpVerificationRepository;

    @Autowired
    IEmailService emailService;

    public void save(UserOtpVerification userOtpVerification) {
        userOtpVerificationRepository.save(userOtpVerification);
    }

    public UserOtpVerification findTopByUserIdAndIpAddressAndUserAgentAndOtpTypeAndUsedAtIsNotNullOrderByUsedAtDesc(Long userId, String ipAddress, String userAgent, String type) {
        return userOtpVerificationRepository.findTopByUserIdAndIpAddressAndUserAgentAndOtpTypeAndUsedAtIsNotNullOrderByUsedAtDesc(userId, ipAddress, userAgent, type).orElse(null);
    }

    public UserOtpVerification findTopByUserIdAndIpAddressAndUserAgentAndOtpTypeOrderByCreatedAtDesc(Long userId, String ipAddress, String userAgent, String type) {
        return userOtpVerificationRepository.findTopByUserIdAndIpAddressAndUserAgentAndOtpTypeOrderByCreatedAtDesc(userId, ipAddress, userAgent, type).orElse(null);
    }

    public UserOtpVerification findLatestSuccessfulLoginOtp(User user, String ipAddress, String userAgent) {
        return findTopByUserIdAndIpAddressAndUserAgentAndOtpTypeAndUsedAtIsNotNullOrderByUsedAtDesc(user.getId(), ipAddress, userAgent, UserOtpVerificationConstants.TYPE_LOGIN);
    }

    public void sendLoginOtp(User user, String ipAddress, String userAgent) {
        UserOtpVerificationConstants.Type type = UserOtpVerificationConstants.Type.LOGIN;

        OtpRequestResult otpRequestResult = sendOtp(user, ipAddress, userAgent, type);

        if (StringUtils.isBlank(otpRequestResult.getOtp())) {
            return;
        }

        UserLoginOtpVerification builder = (UserLoginOtpVerification) UserOtpVerificationConstants.Type.LOGIN.getBuilder();

        emailService.sendHtmlEmail(
                        user.getEmail(),
                        builder.getSubject(),
                        builder.name(user.getFirstName())
                               .otpValidityInMinutes(type.getOtpValidityMinutes())
                               .otpCode(otpRequestResult.getOtp())
                               .build(),
                        builder.getTemplate()
                );
    }

    public void resendLoginOtp(User user, String ipAddress, String userAgent) {
        UserOtpVerificationConstants.Type type = UserOtpVerificationConstants.Type.LOGIN;

        UserOtpVerification userOtpVerification = findTopByUserIdAndIpAddressAndUserAgentAndOtpTypeOrderByCreatedAtDesc(user.getId(), ipAddress, userAgent, type.getType());

        if (userOtpVerification == null) {
            log.info("Otp is not found userId={} , ipAddress={}, userAgent={}", user.getId(), ipAddress, userAgent);
            throw new ApplicationException(INVALID_REQUEST);
        }

        boolean isActive = userOtpVerification.getIsActive();

        if (!isActive) {
            log.info("Otp is inactive userId={} , userOtpVerificationId={}", user.getId(), userOtpVerification.getId());
            throw new ApplicationException(INVALID_REQUEST);
        }

        // Check if OTP is expired (Deactivate it)
        if (isExpired(userOtpVerification.getExpiryDate())) {
            log.info("Otp is expired userId={} , userOtpVerificationId={}", user.getId(), userOtpVerification.getId());
            markAsExpired(userOtpVerification);
            throw new ApplicationException(INVALID_REQUEST);
        }

        if (isOtpVerificationReachedMax(userOtpVerification.getResendCount())) {
            log.info("Otp has reached max userId={} , userOtpVerificationId={}", user.getId(), userOtpVerification.getId());
            throw new ApplicationException(INVALID_REQUEST);
        }

        checkIfRequestedBefore(user, ipAddress, userAgent, UserOtpVerificationConstants.Type.LOGIN);
    }

    public void validateLoginOtp(User user, String otp, String ipAddress, String userAgent) {
        UserOtpVerificationConstants.Type type = UserOtpVerificationConstants.Type.LOGIN;

        UserOtpVerification userOtpVerification = findTopByUserIdAndIpAddressAndUserAgentAndOtpTypeOrderByCreatedAtDesc(user.getId(), ipAddress, userAgent, type.getType());

        if (userOtpVerification == null) {
            log.info("Otp is not found userId={} , ipAddress={}, userAgent={}", user.getId(), ipAddress, userAgent);
            throw new ApplicationException(INVALID_REQUEST);
        }

        boolean isActive = userOtpVerification.getIsActive();

        if (!isActive) {
            log.info("Otp is inactive userId={} , userOtpVerificationId={}", user.getId(), userOtpVerification.getId());
            throw new ApplicationException(INVALID_REQUEST);
        }

        // Check if OTP is expired (Deactivate it)
        if (isExpired(userOtpVerification.getExpiryDate())) {
            log.info("Otp is expired userId={} , userOtpVerificationId={}", user.getId(), userOtpVerification.getId());
            markAsExpired(userOtpVerification);
            throw new ApplicationException(INVALID_REQUEST);
        }

        if (!sha512Hex(otp).equals(userOtpVerification.getOtp())) {
            log.info("Invalid otp userId={} , userOtpVerificationId={}", user.getId(), userOtpVerification.getId());
            throw new ApplicationException(INVALID_REQUEST);
        }

        markAsVerified(userOtpVerification);
    }

    private OtpRequestResult sendOtp(User user, String ipAddress, String userAgent, UserOtpVerificationConstants.Type type) {
        OtpRequestResult otpRequestResult = checkIfRequestedBefore(user, ipAddress, userAgent, type);

        if (!otpRequestResult.getNeedToSendNew()) {
            return otpRequestResult;
        }

        String otp = generateOtp();

        createNewOtp(user, ipAddress, userAgent, type, otp);

        otpRequestResult.setOtp(otp);
        return otpRequestResult;
    }

    private OtpRequestResult checkIfRequestedBefore(User user, String ipAddress, String userAgent, UserOtpVerificationConstants.Type type) {
        OtpRequestResult otpRequestResult = new OtpRequestResult();

        UserOtpVerification userOtpVerification = findTopByUserIdAndIpAddressAndUserAgentAndOtpTypeOrderByCreatedAtDesc(user.getId(), ipAddress, userAgent, type.getType());

        if (userOtpVerification != null && userOtpVerification.getUsedAt() == null) {
            boolean isActive = userOtpVerification.getIsActive();

            // Check if OTP is expired (Deactivate it)
            if (isActive && isExpired(userOtpVerification.getExpiryDate())) {
                markAsExpired(userOtpVerification);
                isActive = userOtpVerification.getIsActive();
            }

            // Check if the user is still in the timeout window (15 minutes lock)
            boolean isStillInTimeoutWindow = userOtpVerification.getCreatedAt().isAfter(nowDate().minusMinutes(type.getCooldownPeriodMinutes()));

            // If OTP is inactive and still within the 15-minute window, block resend
            if (!isActive && isStillInTimeoutWindow) {
                throw new ApplicationException(OTP_COOL_DOWN);
            } else if (!isActive){
                return otpRequestResult;
            }

            // Ensure at least {resendIntervalSeconds} seconds have passed before resending
            if (!nowDate().minusSeconds(type.getResendIntervalSeconds()).isAfter(userOtpVerification.getLastResendAt())) {
                throw new ApplicationException(OTP_RESEND_TOO_FREQUENT);
            }

            // Check if the max resend count is hit (3 resends max)
            if (isOtpVerificationReachedMax(userOtpVerification.getResendCount())) {
                throw new ApplicationException(OTP_REQUEST_MAX);
            }

            String otp = generateOtp();

            // Increment resend count and update last resend timestamp
            updateResendCount(userOtpVerification, type, otp);

            otpRequestResult.setOtp(otp);
            otpRequestResult.setNeedToSendNew(false);
        }

        return otpRequestResult;
    }

    private void createNewOtp(User user, String ipAddress, String userAgent, UserOtpVerificationConstants.Type type, String otp) {
        UserOtpVerification userOtpVerification = new UserOtpVerification();

        userOtpVerification.setUser(user);
        userOtpVerification.setOtp(sha512Hex(otp));
        userOtpVerification.setOtpType(type.getType());

        userOtpVerification.setIsActive(true);
        userOtpVerification.setUserAgent(userAgent);
        userOtpVerification.setIpAddress(ipAddress);
        userOtpVerification.setAttempts(0);
        userOtpVerification.setResendCount(1);

        userOtpVerification.setLastResendAt(nowDate());
        userOtpVerification.setExpiryDate(nowDate().plusMinutes(type.getOtpValidityMinutes()));

        save(userOtpVerification);
    }

    private void updateResendCount(UserOtpVerification userOtpVerification, UserOtpVerificationConstants.Type type, String otp) {
        userOtpVerification.setOtp(sha512Hex(otp));
        userOtpVerification.setResendCount(userOtpVerification.getResendCount() + 1);
        userOtpVerification.setLastResendAt(nowDate());
        userOtpVerification.setExpiryDate(nowDate().plusMinutes(type.getOtpValidityMinutes()));
        save(userOtpVerification);
    }

    private void markAsVerified(UserOtpVerification userOtpVerification) {
        userOtpVerification.setIsActive(false);
        userOtpVerification.setUsedAt(nowDate());
        save(userOtpVerification);
    }

    private void markAsExpired(UserOtpVerification userOtpVerification) {
        userOtpVerification.setIsActive(false);
        save(userOtpVerification);
    }

    private boolean isOtpVerificationReachedMax(Integer resendCount) {
        return resendCount >= 3;
    }

    private boolean isExpired(LocalDateTime expiryDate) {
        return expiryDate.isBefore(nowDate());
    }

    private String generateOtp() {
        Random random = new Random();
        String otp = String.valueOf(100000 + random.nextInt(900000));

        log.info("Otp generated: {}", otp);
        return otp;
    }

    private String sha512Hex(String value) {
        return Sha512DigestUtils.shaHex(value);
    }
}
