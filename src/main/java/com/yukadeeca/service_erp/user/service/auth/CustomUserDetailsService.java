package com.yukadeeca.service_erp.user.service.auth;

import com.yukadeeca.service_erp.common.constant.UserConstants;
import com.yukadeeca.service_erp.user.entity.Role;
import com.yukadeeca.service_erp.user.entity.User;
import com.yukadeeca.service_erp.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserService userService;

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = null;
        try {
            user = userService.findById(Long.parseLong(username));
        } catch (Exception ex) {
            user = userService.findByEmail(username);
        }

        if (user == null || !user.getStatus().equals(UserConstants.STATUS_ACTIVE)) {
            log.info("User not found / not active during login attempt email={} , status={}", username, user != null ? user.getStatus() : null);
            throw new UsernameNotFoundException("User not found");
        }

        //Force lazy intitialize
        user.getRoles().size();

        Set<Role> userRoles = user.getRoles();

        List<GrantedAuthority> authorities = new ArrayList<>();

        for(Role role : userRoles) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getPassword(),
                user.getPassword(),
                authorities
        );
    }
}
