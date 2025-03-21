package com.yukadeeca.service_erp.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yukadeeca.service_erp.common.constant.ErrorCode;
import com.yukadeeca.service_erp.common.exception.ApplicationException;
import com.yukadeeca.service_erp.common.exception.ErrorResponse;
import com.yukadeeca.service_erp.security.dto.TokenResult;
import com.yukadeeca.service_erp.security.dto.UserLoginRequest;
import com.yukadeeca.service_erp.user.service.auth.UserAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final UserAuthService userAuthService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager, UserAuthService userAuthService) {
        super(authenticationManager);
        this.userAuthService = userAuthService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            UserLoginRequest userLoginRequest = objectMapper.readValue(request.getInputStream(), UserLoginRequest.class);

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userLoginRequest.getEmail(),
                    userLoginRequest.getPassword()
            );

            return getAuthenticationManager().authenticate(auth);
        } catch (Exception e) {
            throw new AuthenticationServiceException(e.getMessage(), e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        try {
            User user = (User) authResult.getPrincipal();

            if (userAuthService.isMfaRequired(user.getUsername(), request)) {

                userAuthService.sendOtpVerification(user.getUsername(), request);

                response.setContentType(APPLICATION_JSON_VALUE);
                response.setStatus(HttpServletResponse.SC_OK);

                Map<String, Object> result = new HashMap<>();
                result.put("successMessage", "Need otp verification");
                objectMapper.writeValue(response.getOutputStream(), result);
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
        } catch (Exception ex) {
            log.error("[{}]: Unable to proceed with successful authentication errorMessage={}", ex.getClass().getSimpleName(), ex.getMessage());
            throw new AuthenticationServiceException("Internal Server Error. Please contact support.", ex);
        }
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setContentType(APPLICATION_JSON_VALUE);

        if (failed.getCause() instanceof BadCredentialsException) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INVALID_CREDENTIALS, request.getRequestURI());
            objectMapper.writeValue(response.getOutputStream(), errorResponse);
            return;
        }

        if (failed.getCause() instanceof ApplicationException) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse(((ApplicationException) failed.getCause()).getErrorCode(), request.getRequestURI());
            objectMapper.writeValue(response.getOutputStream(), errorResponse);
            return;
        }

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, request.getRequestURI());
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
