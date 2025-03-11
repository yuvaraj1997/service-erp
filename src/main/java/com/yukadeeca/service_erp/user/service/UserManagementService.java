package com.yukadeeca.service_erp.user.service;


import com.yukadeeca.service_erp.common.constant.RoleConstants;
import com.yukadeeca.service_erp.common.constant.UserConstants;
import com.yukadeeca.service_erp.common.constant.VerificationTokenConstants;
import com.yukadeeca.service_erp.common.exception.InvalidArgumentException;
import com.yukadeeca.service_erp.user.dto.UserCreateRequest;
import com.yukadeeca.service_erp.user.dto.UserSetPasswordRequest;
import com.yukadeeca.service_erp.user.entity.User;
import com.yukadeeca.service_erp.user.entity.VerificationToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class UserManagementService {

    @Autowired
    UserService userService;

    @Autowired
    RoleService roleService;

    @Autowired
    VerificationTokenService verificationTokenService;

    @Autowired
    PasswordEncoder passwordEncoder;

    public void createSystemUserWithVerification(UserCreateRequest userCreateRequest) {
        createUserWithVerification(userCreateRequest, null);
    }

    public User createUserWithVerification(UserCreateRequest userCreateRequest, String role) {

        User user = userService.findByEmail(userCreateRequest.getEmail());

        if (null != user) {
            return handleExistingUser(user, userCreateRequest);
        }

        user = new User();
        user.setEmail(userCreateRequest.getEmail());
        user.setFirstName(userCreateRequest.getFirstName());
        user.setStatus(UserConstants.STATUS_PENDING_SETUP);

        //If role not supplied default to User
        if (StringUtils.isBlank(role)) {
            role = RoleConstants.USER;
        }

        user.getRoles().add(roleService.findByName(role));

        User savedUser = userService.save(user);

        verificationTokenService.sendEmailVerificationEmail(savedUser);

        return savedUser;
    }

    private User handleExistingUser(User existingUser, UserCreateRequest userCreateRequest) {

        String status = existingUser.getStatus();

        switch (status) {
            case UserConstants.STATUS_ACTIVE:
                log.info("User already active: userId={}, email={}",
                        existingUser.getId(), existingUser.getEmail());
                return existingUser;

            case UserConstants.STATUS_PENDING_SETUP:
                existingUser.setFirstName(userCreateRequest.getFirstName());
                existingUser.setLastName(userCreateRequest.getLastName());
                userService.save(existingUser);
                verificationTokenService.sendEmailVerificationEmail(existingUser);
                log.info("Updated and resent verification for pending user: userId={}, status={}",
                        existingUser.getId(), status);
                return existingUser;

            default:
                log.warn("User is already exist, but didn't satisfy any requirement to proceed so early exit userId={} , status={}", existingUser.getId(), existingUser.getStatus());
                return null;
        }
    }

    public void setPassword(UserSetPasswordRequest userSetPasswordRequest) {
        VerificationToken verificationToken = verificationTokenService.verifyToken(userSetPasswordRequest.getToken(), VerificationTokenConstants.TYPE_EMAIL_VERIFICATION);

        User user = verificationToken.getUser();

        if (!Objects.equals(user.getStatus(), UserConstants.STATUS_PENDING_SETUP)) {
            log.info("User status is invalid to set password userId={} , expected={} , currentStatus={}", user.getId(), UserConstants.STATUS_PENDING_SETUP, user.getStatus());
            throw new InvalidArgumentException("Invalid request");
        }

        user.setPassword(passwordEncoder.encode(userSetPasswordRequest.getPassword()));
        user.setStatus(UserConstants.STATUS_ACTIVE);
        userService.save(user);

        log.info("Successfully set password userId={} , token={}", user.getId(), verificationToken.getToken());
    }
}
