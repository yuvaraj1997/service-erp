package com.yukadeeca.service_erp.user.service;


import com.yukadeeca.service_erp.user.entity.Role;
import com.yukadeeca.service_erp.user.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    @Autowired
    RoleRepository roleRepository;

    public Role findByName(String name) {
        return roleRepository.findByName(name);
    }
}
