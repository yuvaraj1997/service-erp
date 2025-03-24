package com.yukadeeca.service_erp.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yukadeeca.service_erp.common.constant.SuccessCode;
import com.yukadeeca.service_erp.common.dto.SuccessResponse;
import com.yukadeeca.service_erp.security.dto.TokenResult;
import com.yukadeeca.service_erp.user.dto.otp.OtpRequestResponse;
import com.yukadeeca.service_erp.user.service.auth.UserAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class MFAAuthenticationFilter extends OncePerRequestFilter {

    private final UserAuthService userAuthService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public MFAAuthenticationFilter(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (null != authentication && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();

            if (userAuthService.isMfaRequired(user.getUsername(), request)) {

                OtpRequestResponse otpRequestResponse = userAuthService.sendOtpVerification(user.getUsername(), request);

                response.setContentType(APPLICATION_JSON_VALUE);

                SuccessResponse<OtpRequestResponse> successResponse = new SuccessResponse<>(SuccessCode.OTP_REQUESTED);
                successResponse.setData(otpRequestResponse);

                response.setStatus(successResponse.getStatus());
                objectMapper.writeValue(response.getOutputStream(), successResponse);
                return;
            }

            TokenResult tokenResult = userAuthService.generateRefreshToken(user.getUsername());

            Cookie refreshTokenCookie = new Cookie("token", tokenResult.getRefreshToken());
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(tokenResult.getExpiresInSeconds().intValue());

            response.addCookie(refreshTokenCookie);
            response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
            response.sendRedirect("http://localhost:3000");
            return;
        }

        filterChain.doFilter(request, response);
    }


}
