package com.yukadeeca.service_erp.user.controller;

import com.yukadeeca.service_erp.user.dto.UserCreateRequest;
import com.yukadeeca.service_erp.user.dto.UserSetPasswordRequest;
import com.yukadeeca.service_erp.user.entity.User;
import com.yukadeeca.service_erp.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping
    public ResponseEntity<User> createNewUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        userService.createSystemUserWithVerification(userCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/setPassword")
    public ResponseEntity<?> setPassword(@Valid @RequestBody UserSetPasswordRequest userSetPasswordRequest) {
        userService.setPassword(userSetPasswordRequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
