package com.yukadeeca.service_erp.user.service.auth;


import com.yukadeeca.service_erp.common.constant.JwtConstants;
import com.yukadeeca.service_erp.common.exception.ApplicationException;
import com.yukadeeca.service_erp.common.util.DeviceInfoUtil;
import com.yukadeeca.service_erp.security.dto.AuthorizationToken;
import com.yukadeeca.service_erp.security.dto.TokenResult;
import com.yukadeeca.service_erp.security.util.JwtUtil;
import com.yukadeeca.service_erp.user.dto.otp.OtpRequestResponse;
import com.yukadeeca.service_erp.user.entity.Role;
import com.yukadeeca.service_erp.user.entity.User;
import com.yukadeeca.service_erp.user.entity.UserOtpVerification;
import com.yukadeeca.service_erp.user.service.UserOtpVerificationService;
import com.yukadeeca.service_erp.user.service.UserService;
import com.yukadeeca.service_erp.user.service.UserSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.yukadeeca.service_erp.common.constant.ErrorCode.INVALID_REQUEST;
import static com.yukadeeca.service_erp.common.util.DateUtil.nowDate;

@Slf4j
@Service
public class UserAuthService {

    @Autowired
    UserService userService;

    @Autowired
    UserSessionService userSessionService;

    @Autowired
    UserOtpVerificationService userOtpVerificationService;

    @Autowired
    JwtUtil jwtUtil;

    @Transactional
    public TokenResult generateRefreshToken(String email) {
        User user = userService.findByEmail(email);
        Set<Role> userRoles = user.getRoles();

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstants.CLAIMS_ROLES, userRoles.stream().map(Role::getName).collect(Collectors.toList()));

        TokenResult tokenResult = jwtUtil.generateRefreshToken(user.getId().toString(), claims);

        userSessionService.saveUserSession(user, tokenResult.getJti(), tokenResult.getIssuedAt(), tokenResult.getExpiration());

        return tokenResult;
    }

    public TokenResult generateAccessToken(AuthorizationToken authorizationToken) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtConstants.CLAIMS_ROLES, authorizationToken.getUserRoles());

        return jwtUtil.generateAccessToken(authorizationToken.getUserId().toString(), claims, authorizationToken.getSessionId());
    }

    public boolean isMfaRequired(String email, HttpServletRequest request) {
        User user = userService.findByEmail(email);

        String ipAddress = DeviceInfoUtil.getClientIp(request);
        String userAgent = DeviceInfoUtil.getUserDevice(request);

        UserOtpVerification userOtpVerification = userOtpVerificationService.findLatestSuccessfulLoginOtp(user, ipAddress, userAgent);

        if (userOtpVerification == null) {
            return true;
        }

        return userOtpVerification.getUsedAt().isBefore(nowDate().minusDays(2));
    }

    public OtpRequestResponse sendOtpVerification(String email, HttpServletRequest request) {
        User user = userService.findByEmail(email);

        String ipAddress = DeviceInfoUtil.getClientIp(request);
        String userAgent = DeviceInfoUtil.getUserDevice(request);

        return userOtpVerificationService.sendLoginOtp(user, ipAddress, userAgent);
    }

    public OtpRequestResponse resendOtpVerification(String email, HttpServletRequest request) {
        User user = userService.findByEmail(email);

        if (null == user) {
            log.info("User not found to send otp verification email={} , requestUri={}", email, request.getRequestURI());
            return null;
        }

        String ipAddress = DeviceInfoUtil.getClientIp(request);
        String userAgent = DeviceInfoUtil.getUserDevice(request);

        return userOtpVerificationService.resendLoginOtp(user, ipAddress, userAgent);
    }

    public void validateOtpVerification(String email, String otp, HttpServletRequest request) {
        User user = userService.findByEmail(email);

        if (null == user) {
            log.info("User not found to validate otp verification email={} , requestUri={}", email, request.getRequestURI());
            throw new ApplicationException(INVALID_REQUEST);
        }

        String ipAddress = DeviceInfoUtil.getClientIp(request);
        String userAgent = DeviceInfoUtil.getUserDevice(request);

        userOtpVerificationService.validateLoginOtp(user, otp, ipAddress, userAgent);
    }
}
