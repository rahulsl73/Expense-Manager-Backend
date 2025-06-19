package com.example.expensemanager.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.expensemanager.dto.AuthRequest;
import com.example.expensemanager.dto.AuthResponse;
import com.example.expensemanager.dto.SignupRequest;
import com.example.expensemanager.dto.UserDto;
import com.example.expensemanager.model.User;
import com.example.expensemanager.security.JwtUtil;
import com.example.expensemanager.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager auth;
    private final UserService userSvc;
    private final JwtUtil jwt;

    public AuthController(AuthenticationManager auth, JwtUtil jwt, UserService userSvc) {
        this.auth = auth;
        this.jwt = jwt;
        this.userSvc = userSvc;
    }

    // pass directly user with dto
    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(
            @Valid @RequestBody SignupRequest req) {

        // map request → entity
        User toCreate = new User();
        toCreate.setUsername(req.getUsername());
        toCreate.setEmail(req.getEmail());
        toCreate.setPassword(req.getPassword());

        User created = userSvc.register(toCreate);

        // map entity → DTO (hiding password, secret)
        UserDto dto = UserDto.from(created);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(dto);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest req) {

        try {
            auth.authenticate(
                new UsernamePasswordAuthenticationToken(
                    req.getUsername(), req.getPassword()
                )
            );
        } catch (BadCredentialsException ex) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        }

        String token = jwt.generateToken(req.getUsername());
        return ResponseEntity.ok(new AuthResponse(token));
    }
 
}
