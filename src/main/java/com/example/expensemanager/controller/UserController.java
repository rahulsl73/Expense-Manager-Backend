package com.example.expensemanager.controller;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.expensemanager.model.User;
import com.example.expensemanager.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userSvc;
    public UserController(UserService u){
        this.userSvc = u;
    }

    @PutMapping("/budget")
    public ResponseEntity<User> setBudget(@RequestHeader("User-Id") Long uid, @RequestBody BigDecimal b){
        return ResponseEntity.ok(userSvc.updateBudget(uid, b));
    }
}
