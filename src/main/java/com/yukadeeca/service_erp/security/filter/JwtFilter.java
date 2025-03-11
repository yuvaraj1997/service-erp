package com.yukadeeca.service_erp.security.filter;

import com.yukadeeca.service_erp.common.constant.JwtConstants;
import com.yukadeeca.service_erp.security.dto.AuthorizationToken;
import com.yukadeeca.service_erp.security.util.JwtUtil;
import com.yukadeeca.service_erp.user.service.UserSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final String API_TO_GET_ACCESS_TOKEN = "/auth/token";

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    private UserSessionService userSessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        Optional<Cookie> cookieToken = Optional.empty();

        if (null != request.getCookies()) {
            cookieToken = Arrays.stream(request.getCookies()).filter(cookie -> Objects.equals(cookie.getName(), "token")).findFirst();
        }

        String token = cookieToken.map(Cookie::getValue).map(t -> t.replace("Bearer ", "")).orElse(null);

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null) {
            token = authorizationHeader.replace("Bearer ", "");
        }

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        AuthorizationToken authorizationToken = jwtUtil.parseToken(token);

        if (!jwtUtil.isTokenValid(token)) {
            userSessionService.markSessionAsInactive(authorizationToken.getUserId(), authorizationToken.getSessionId());
            removeCookie(response, "token");
            filterChain.doFilter(request, response);
            return;
        }

        if (!userSessionService.isSessionActive(authorizationToken.getUserId(), authorizationToken.getSessionId())) {
            log.info("Session expired userId={} , sessionId={}", authorizationToken.getUserId(), authorizationToken.getSessionId());
            removeCookie(response, "token");
            filterChain.doFilter(request, response);
            return;
        }

        String tokenType = authorizationToken.getTokenType();

        boolean isAllowedToken = (tokenType.equals(JwtConstants.TokenType.REFRESH_TOKEN.getType()) && uri.equals(API_TO_GET_ACCESS_TOKEN)) ||
                (tokenType.equals(JwtConstants.TokenType.ACCESS_TOKEN.getType()) && !uri.equals(API_TO_GET_ACCESS_TOKEN));

        if (!isAllowedToken) {
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                authorizationToken, null, authorizationToken.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    public void removeCookie(HttpServletResponse response, String cookieName) {
        Cookie refreshTokenCookie = new Cookie(cookieName, "");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);

        response.addCookie(refreshTokenCookie);
    }
}
