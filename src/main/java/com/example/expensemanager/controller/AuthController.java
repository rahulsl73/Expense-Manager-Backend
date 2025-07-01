package com.example.expensemanager.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.expensemanager.constant.Constants;
import com.example.expensemanager.dto.AuthRequest;
import com.example.expensemanager.dto.AuthResponse;
import com.example.expensemanager.dto.SignupRequest;
import com.example.expensemanager.dto.UserDto;
import com.example.expensemanager.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;


@RestController
@RequestMapping(Constants.AUTH_BASE)
public class AuthController {
    private final AuthService authSvc;

    public AuthController(AuthService authSvc) {
        this.authSvc = authSvc;
    }

    @PostMapping(Constants.SIGNUP)
    public ResponseEntity<UserDto> signup(@Valid @RequestBody SignupRequest req) {
        UserDto created = authSvc.signup(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping(Constants.LOGIN)
    public AuthResponse login(HttpServletResponse response,@Valid @RequestBody AuthRequest req) {
        return authSvc.login(response,req);
    }
}