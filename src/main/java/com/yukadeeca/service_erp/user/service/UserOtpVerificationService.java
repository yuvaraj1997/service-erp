package com.yukadeeca.service_erp.user.service;


import com.yukadeeca.service_erp.common.constant.UserOtpVerificationConstants;
import com.yukadeeca.service_erp.common.constant.emailTemplate.UserLoginOtpVerification;
import com.yukadeeca.service_erp.common.exception.ApplicationException;
import com.yukadeeca.service_erp.common.service.email.IEmailService;
import com.yukadeeca.service_erp.user.dto.otp.OtpRequestResponse;
import com.yukadeeca.service_erp.user.dto.otp.OtpRequestResult;
import com.yukadeeca.service_erp.user.entity.User;
import com.yukadeeca.service_erp.user.entity.UserOtpVerification;
import com.yukadeeca.service_erp.user.repository.UserOtpVerificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.stereotype.Service;

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

    public OtpRequestResponse sendLoginOtp(User user, String ipAddress, String userAgent) {
        return sendOtp(user, ipAddress, userAgent, UserOtpVerificationConstants.Type.LOGIN);
    }

    public OtpRequestResponse resendLoginOtp(User user, String ipAddress, String userAgent) {
        return resendOtp(user, ipAddress, userAgent, UserOtpVerificationConstants.Type.LOGIN);
    }

    public void validateLoginOtp(User user, String otp, String ipAddress, String userAgent) {
        validateOtp(user, otp, ipAddress, userAgent, UserOtpVerificationConstants.Type.LOGIN);
    }

    private void sendLoginOtpEmail(User user, String otp, UserOtpVerificationConstants.Type type) {
        UserLoginOtpVerification builder = (UserLoginOtpVerification) type.getBuilder();

        emailService.sendHtmlEmail(
                user.getEmail(),
                builder.getSubject(),
                builder.name(user.getFirstName())
                        .otpValidityInMinutes(type.getOtpValidityMinutes())
                        .otpCode(otp)
                        .build(),
                builder.getTemplate()
        );
    }

    private OtpRequestResponse sendOtp(User user, String ipAddress, String userAgent, UserOtpVerificationConstants.Type type) {
        OtpRequestResult otpRequestResult = checkIfRequestedBefore(user, ipAddress, userAgent, type);

        String otp = null;

        if (!otpRequestResult.getNeedToSendNew()) {
            otp = otpRequestResult.getOtp();
        } else {
            otp = generateOtp();

            createNewOtp(user, ipAddress, userAgent, type, otp);

            otpRequestResult.setOtp(otp);
            otpRequestResult.setMaxRequestLimit(type.getMaxRequest());
            otpRequestResult.setRemainingRequestCount(type.getMaxRequest());
            otpRequestResult.setRetryAfterSeconds(type.getResendIntervalSeconds());
        }

        if (StringUtils.isNotBlank(otp)) {
            sendOtpEmail(user, otp, type);
        }

        return buildOtpRequestResponse(otpRequestResult);
    }

    public OtpRequestResponse resendOtp(User user, String ipAddress, String userAgent, UserOtpVerificationConstants.Type type) {
        UserOtpVerification userOtpVerification = findTopByUserIdAndIpAddressAndUserAgentAndOtpTypeOrderByCreatedAtDesc(user.getId(), ipAddress, userAgent, type.getType());

        basicValidation(user, userOtpVerification, ipAddress, userAgent);

        if (isOtpVerificationReachedMax(type, userOtpVerification.getResendCount())) {
            log.info("Otp has reached max userId={} , userOtpVerificationId={}", user.getId(), userOtpVerification.getId());
            throw new ApplicationException(INVALID_REQUEST);
        }

        OtpRequestResult otpRequestResult = checkIfRequestedBefore(user, ipAddress, userAgent, UserOtpVerificationConstants.Type.LOGIN);

        if (StringUtils.isNotBlank(otpRequestResult.getOtp())) {
            sendOtpEmail(user, otpRequestResult.getOtp(), type);
        }

        return buildOtpRequestResponse(otpRequestResult);
    }

    private OtpRequestResult checkIfRequestedBefore(User user, String ipAddress, String userAgent, UserOtpVerificationConstants.Type type) {
        OtpRequestResult otpRequestResult = new OtpRequestResult();

        UserOtpVerification userOtpVerification = findTopByUserIdAndIpAddressAndUserAgentAndOtpTypeOrderByCreatedAtDesc(user.getId(), ipAddress, userAgent, type.getType());

        if (userOtpVerification != null && userOtpVerification.getUsedAt() == null) {
            boolean isActive = userOtpVerification.getIsActive();

            // Check if OTP is expired (Deactivate it)
            if (isActive && isExpired(user, userOtpVerification)) {
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
            if (isOtpVerificationReachedMax(type, userOtpVerification.getResendCount())) {
                throw new ApplicationException(OTP_REQUEST_MAX);
            }

            String otp = generateOtp();

            // Increment resend count and update last resend timestamp
            updateResendCount(userOtpVerification, type, otp);

            otpRequestResult.setOtp(otp);
            otpRequestResult.setNeedToSendNew(false);
            otpRequestResult.setMaxRequestLimit(type.getMaxRequest());
            otpRequestResult.setRemainingRequestCount(type.getMaxRequest() - userOtpVerification.getResendCount());
            otpRequestResult.setRetryAfterSeconds(type.getResendIntervalSeconds());
        }

        return otpRequestResult;
    }

    private void sendOtpEmail(User user, String otp, UserOtpVerificationConstants.Type type) {
        switch (type) {
            case LOGIN -> sendLoginOtpEmail(user, otp, type);
            default -> throw new RuntimeException("OTP Email sending type is not configured");
        }

    }

    public void validateOtp(User user, String otp, String ipAddress, String userAgent, UserOtpVerificationConstants.Type type) {
        UserOtpVerification userOtpVerification = findTopByUserIdAndIpAddressAndUserAgentAndOtpTypeOrderByCreatedAtDesc(user.getId(), ipAddress, userAgent, type.getType());

        basicValidation(user, userOtpVerification, ipAddress, userAgent);

        if (!sha512Hex(otp).equals(userOtpVerification.getOtp())) {
            log.info("Invalid otp userId={} , userOtpVerificationId={}", user.getId(), userOtpVerification.getId());
            throw new ApplicationException(INVALID_REQUEST);
        }

        markAsVerified(userOtpVerification);
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

    private void basicValidation(User user, UserOtpVerification userOtpVerification, String ipAddress, String userAgent) {
        if (userOtpVerification == null) {
            log.info("Otp is not found userId={} , ipAddress={}, userAgent={}", user.getId(), ipAddress, userAgent);
            throw new ApplicationException(INVALID_REQUEST);
        }

        if (isInactive(user,  userOtpVerification)) {
            throw new ApplicationException(INVALID_REQUEST);
        }

        // Check if OTP is expired (Deactivate it)
        if (isExpired(user, userOtpVerification)) {
            throw new ApplicationException(INVALID_REQUEST);
        }
    }

    private void markAsExpired(UserOtpVerification userOtpVerification) {
        userOtpVerification.setIsActive(false);
        save(userOtpVerification);
    }

    private boolean isOtpVerificationReachedMax(UserOtpVerificationConstants.Type type, Integer resendCount) {
        return resendCount >= type.getMaxRequest();
    }

    private boolean isExpired(User user, UserOtpVerification userOtpVerification) {
        if (!userOtpVerification.getExpiryDate().isBefore(nowDate())) {
            return false;
        }

        log.info("Otp is expired userId={} , userOtpVerificationId={}", user.getId(), userOtpVerification.getId());
        markAsExpired(userOtpVerification);
        return true;
    }

    private boolean isInactive(User user, UserOtpVerification userOtpVerification) {
        if (userOtpVerification.getIsActive()) {
            return false;
        }
        log.info("Otp is inactive userId={} , userOtpVerificationId={}", user.getId(), userOtpVerification.getId());
        return true;
    }

    private OtpRequestResponse buildOtpRequestResponse(OtpRequestResult otpRequestResult) {
        return new OtpRequestResponse(
                otpRequestResult.getRemainingRequestCount(),
                otpRequestResult.getMaxRequestLimit(),
                otpRequestResult.getRetryAfterSeconds()
        );
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
