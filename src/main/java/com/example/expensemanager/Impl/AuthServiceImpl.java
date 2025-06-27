package com.example.expensemanager.Impl;


import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.expensemanager.dto.AuthRequest;
import com.example.expensemanager.dto.AuthResponse;
import com.example.expensemanager.dto.SignupRequest;
import com.example.expensemanager.dto.UserDto;
import com.example.expensemanager.exception.DuplicateEmailException;
import com.example.expensemanager.exception.DuplicateUsernameException;
import com.example.expensemanager.exception.InvalidCredentialsException;
import com.example.expensemanager.model.User;
import com.example.expensemanager.security.JwtUtil;
import com.example.expensemanager.service.AuthService;
import com.example.expensemanager.service.UserService;

@Service
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserService userSvc;

    public AuthServiceImpl(
        AuthenticationManager authManager,
        JwtUtil jwtUtil,
        UserService userSvc
    ) {
        this.authManager = authManager;
        this.jwtUtil     = jwtUtil;
        this.userSvc     = userSvc;
    }

    @Override
    @Transactional
    public UserDto signup(SignupRequest req) {
        try {
            User toCreate = new User();
            toCreate.setUsername(req.getUsername());
            toCreate.setEmail(req.getEmail());
            toCreate.setPassword(req.getPassword());

            User created = userSvc.register(toCreate);
            return UserDto.from(created);

        } catch (DataIntegrityViolationException ex) {
            String cause = ex.getMostSpecificCause().getMessage().toLowerCase();
            if (cause.contains("email")) {
                throw new DuplicateEmailException(req.getEmail());
            } else if (cause.contains("username")) {
                throw new DuplicateUsernameException(req.getUsername());
            }
            throw ex;
        }
    }

    @Override
    public AuthResponse login(AuthRequest req) {
        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    req.getUsername(),
                    req.getPassword()
                )
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException();
        }

        User user = userSvc
            .findByUsername(req.getUsername())
            .orElseThrow(() -> 
                new IllegalStateException("Authenticated user not found")
            );

        String token = jwtUtil.generateToken(req.getUsername());
        return new AuthResponse(token, user.getId(), user.getEmail());
    }
}
