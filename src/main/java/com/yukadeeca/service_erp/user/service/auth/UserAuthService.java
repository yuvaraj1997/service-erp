package com.yukadeeca.service_erp.user.service.auth;


import com.yukadeeca.service_erp.common.constant.JwtConstants;
import com.yukadeeca.service_erp.security.dto.AuthorizationToken;
import com.yukadeeca.service_erp.security.dto.TokenResult;
import com.yukadeeca.service_erp.security.util.JwtUtil;
import com.yukadeeca.service_erp.user.entity.Role;
import com.yukadeeca.service_erp.user.entity.User;
import com.yukadeeca.service_erp.user.service.UserService;
import com.yukadeeca.service_erp.user.service.UserSessionService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserAuthService {

    @Autowired
    UserService userService;

    @Autowired
    UserSessionService userSessionService;

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
}
