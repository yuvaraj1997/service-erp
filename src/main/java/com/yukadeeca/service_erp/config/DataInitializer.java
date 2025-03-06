package com.yukadeeca.service_erp.config;

import com.yukadeeca.service_erp.common.constant.RoleConstants;
import com.yukadeeca.service_erp.common.service.email.IEmailService;
import com.yukadeeca.service_erp.user.dto.UserCreateRequest;
import com.yukadeeca.service_erp.user.entity.User;
import com.yukadeeca.service_erp.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${system.superadmin.email}")
    private String superAdminEmail;

    @Bean
    @Transactional
    public ApplicationRunner initSystemUsers(UserService userService, IEmailService emailService) {
        return args -> {

            User user = userService.findByEmail(superAdminEmail);

            if (null == user) {

                UserCreateRequest userCreateRequest = new UserCreateRequest();
                userCreateRequest.setEmail(superAdminEmail);
                userCreateRequest.setFirstName("Admin");
                userCreateRequest.setLastName("User");

                User adminUser = userService.createUserWithVerification(userCreateRequest, RoleConstants.SUPER_ADMIN);

                log.info("Admin user is successfully create id={} , email={}", adminUser.getId(), superAdminEmail);
            }
        };
    }

}
