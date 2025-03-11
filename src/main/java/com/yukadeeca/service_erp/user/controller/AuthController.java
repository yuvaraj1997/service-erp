package com.yukadeeca.service_erp.user.controller;

import com.yukadeeca.service_erp.security.dto.AuthorizationToken;
import com.yukadeeca.service_erp.security.dto.TokenResult;
import com.yukadeeca.service_erp.user.dto.UserLoginRequest;
import com.yukadeeca.service_erp.user.service.auth.UserAuthService;
import jakarta.servlet.http.Cookie;
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

    @PostMapping("/login")
    public void login(@Valid @RequestBody UserLoginRequest userLoginRequest, HttpServletResponse httpServletResponse) throws IOException {
        TokenResult tokenResult = userAuthService.login(userLoginRequest);

        Cookie refreshTokenCookie = new Cookie("token", tokenResult.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(tokenResult.getExpiresInSeconds().intValue());

        httpServletResponse.addCookie(refreshTokenCookie);

        httpServletResponse.sendRedirect("http://localhost:3000");
    }

    @GetMapping("/token")
    public ResponseEntity<?> getAccessToken(@AuthenticationPrincipal AuthorizationToken authorizationToken) throws IOException {
        return ResponseEntity.ok().body(userAuthService.generateAccessToken(authorizationToken));
    }
}
