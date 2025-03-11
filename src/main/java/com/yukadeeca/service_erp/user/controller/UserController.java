package com.yukadeeca.service_erp.user.controller;

import com.yukadeeca.service_erp.user.dto.UserCreateRequest;
import com.yukadeeca.service_erp.user.dto.UserSetPasswordRequest;
import com.yukadeeca.service_erp.user.entity.User;
import com.yukadeeca.service_erp.user.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users")
public class UserController {

    @Autowired
    UserManagementService userManagementService;

    @GetMapping
    public ResponseEntity<User> protectedtest() {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PostMapping
    public ResponseEntity<User> createNewUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        userManagementService.createSystemUserWithVerification(userCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/setPassword")
    public ResponseEntity<?> setPassword(@Valid @RequestBody UserSetPasswordRequest userSetPasswordRequest) {
        userManagementService.setPassword(userSetPasswordRequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
