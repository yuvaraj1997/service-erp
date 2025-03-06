package com.yukadeeca.service_erp.user.repository;

import com.yukadeeca.service_erp.user.entity.Role;
import com.yukadeeca.service_erp.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    // Find a role by its name
    Role findByName(String name);

}
