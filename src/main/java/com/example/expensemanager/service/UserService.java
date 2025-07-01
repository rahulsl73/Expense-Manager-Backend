package com.example.expensemanager.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.example.expensemanager.model.User;

public interface UserService extends UserDetailsService {
    User register(User u);
    User findByIdOrThrow(Long id);
    Optional<User> findByUsername(String username);
    User updateProfile(Long uid, String newEmail, BigDecimal newBudget);
}