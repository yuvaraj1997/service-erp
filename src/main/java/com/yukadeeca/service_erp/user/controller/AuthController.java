package com.yukadeeca.service_erp.user.controller;

import com.yukadeeca.service_erp.security.dto.AuthorizationToken;
import com.yukadeeca.service_erp.security.dto.TokenResult;
import com.yukadeeca.service_erp.user.dto.ResendOtpRequest;
import com.yukadeeca.service_erp.user.dto.ValidateOtpRequest;
import com.yukadeeca.service_erp.user.service.auth.UserAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("auth")
public class AuthController {

    @Autowired
    UserAuthService userAuthService;

    @PostMapping("/otp/resend")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody ResendOtpRequest resendOtpRequest, HttpServletRequest httpServletRequest) throws IOException {
        userAuthService.resendOtpVerification(resendOtpRequest.getEmail(), httpServletRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/otp/validate")
    public ResponseEntity<Void> validateOtp(@Valid @RequestBody ValidateOtpRequest validateOtpRequest, HttpServletRequest httpServletRequest, HttpServletResponse response) throws IOException {
        userAuthService.validateOtpVerification(validateOtpRequest.getEmail(), validateOtpRequest.getOtp(), httpServletRequest);

        TokenResult tokenResult = userAuthService.generateRefreshToken(validateOtpRequest.getEmail());

        Cookie refreshTokenCookie = new Cookie("token", tokenResult.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(tokenResult.getExpiresInSeconds().intValue());
        response.addCookie(refreshTokenCookie);

        response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        response.setHeader("Location", "http://localhost:3000/dashboard");
        return null;
    }

    @GetMapping("/token")
    public ResponseEntity<?> getAccessToken(@AuthenticationPrincipal AuthorizationToken authorizationToken) throws IOException {
        return ResponseEntity.ok().body(userAuthService.generateAccessToken(authorizationToken));
    }
}
