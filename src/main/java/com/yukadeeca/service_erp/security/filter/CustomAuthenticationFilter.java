package com.yukadeeca.service_erp.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yukadeeca.service_erp.common.constant.ErrorCode;
import com.yukadeeca.service_erp.common.exception.ApplicationException;
import com.yukadeeca.service_erp.common.dto.ErrorResponse;
import com.yukadeeca.service_erp.security.dto.UserLoginRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
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
            // 1. Set SecurityContext manually
            SecurityContextHolder.getContext().setAuthentication(authResult);

            // 2. Continue chain (important!)
            chain.doFilter(request, response);
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
