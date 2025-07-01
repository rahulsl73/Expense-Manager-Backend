package com.example.expensemanager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.expensemanager.dto.UserDto;
import com.example.expensemanager.model.User;
import com.example.expensemanager.service.UserService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/user/{userId}")
public class UserController {
    private final UserService userSvc;
    public UserController(UserService u){
        this.userSvc = u;
    }


    @GetMapping("/profile")
    public ResponseEntity<UserDto> getProfile(
        @PathVariable Long userId
    ) {
        User u = userSvc.findByIdOrThrow(userId);
        return ResponseEntity.ok(UserDto.from(u));
    }


    @PutMapping("/profile")   
    public ResponseEntity<UserDto> updateProfile(
       @PathVariable Long userId,
        @Valid @RequestBody UserDto req
    ) {
        var updated = userSvc.updateProfile(userId, req.getEmail(), req.getMonthlyBudget());
        return ResponseEntity
          .ok(UserDto.from(updated));
    }
}
