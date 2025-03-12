package com.yukadeeca.service_erp.user.controller;

import com.yukadeeca.service_erp.security.dto.AuthorizationToken;
import com.yukadeeca.service_erp.user.service.auth.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("auth")
public class AuthController {

    @Autowired
    UserAuthService userAuthService;

    @GetMapping("/token")
    public ResponseEntity<?> getAccessToken(@AuthenticationPrincipal AuthorizationToken authorizationToken) throws IOException {
        return ResponseEntity.ok().body(userAuthService.generateAccessToken(authorizationToken));
    }
}
