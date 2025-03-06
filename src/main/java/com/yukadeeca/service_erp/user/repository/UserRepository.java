package com.yukadeeca.service_erp.user.repository;

import com.yukadeeca.service_erp.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email
    User findByEmail(String email);

}
