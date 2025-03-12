package com.yukadeeca.service_erp.security.dto;

import com.yukadeeca.service_erp.common.constant.JwtConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class AuthorizationToken implements UserDetails {

    private final JwsHeader header;
    private final Claims claims;

    public AuthorizationToken(JwsHeader header, Claims claims) {
        this.header = header;
        this.claims = claims;
    }

    public Long getUserId() {
        return Long.parseLong(claims.getSubject());
    }

    public String getTokenType() {
        return header.getType();
    }

    public String getSessionId() {
        return claims.getId();
    }

    public List<String> getUserRoles() {
        return claims.containsKey(JwtConstants.CLAIMS_ROLES)
                ? (List<String>) claims.get(JwtConstants.CLAIMS_ROLES)
                : new ArrayList<>();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<String> userRoles = getUserRoles();

        List<GrantedAuthority> authorities = new ArrayList<>();

        for (String role : userRoles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return "";
    }
}
