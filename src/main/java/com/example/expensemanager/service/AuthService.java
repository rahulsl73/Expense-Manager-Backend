package com.example.expensemanager.service;

import com.example.expensemanager.dto.AuthRequest;
import com.example.expensemanager.dto.AuthResponse;
import com.example.expensemanager.dto.SignupRequest;
import com.example.expensemanager.dto.UserDto;

public interface AuthService {
    
    UserDto signup(SignupRequest req);

    AuthResponse login(AuthRequest req);
}