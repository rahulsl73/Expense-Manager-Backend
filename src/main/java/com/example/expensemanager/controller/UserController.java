package com.example.expensemanager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.expensemanager.dto.UserDto;
import com.example.expensemanager.model.User;
import com.example.expensemanager.service.UserService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userSvc;
    public UserController(UserService u){
        this.userSvc = u;
    }


    @GetMapping("/profile")
    public ResponseEntity<UserDto> getProfile(
        @RequestHeader("User-Id") Long uid
    ) {
        User u = userSvc.findByIdOrThrow(uid);
        return ResponseEntity.ok(UserDto.from(u));
    }


    @PutMapping("/profile")   
    public ResponseEntity<UserDto> updateProfile(
        @RequestHeader("User-Id") Long uid,
        @Valid @RequestBody UserDto req
    ) {
        var updated = userSvc.updateProfile(uid, req.getEmail(), req.getMonthlyBudget());
        return ResponseEntity
          .ok(UserDto.from(updated));
    }
}
