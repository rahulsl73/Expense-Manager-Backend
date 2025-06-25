package com.example.expensemanager.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
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
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserService userSvc;

    public AuthController(AuthenticationManager authManager,
                          JwtUtil jwtUtil,
                          UserService userSvc) {
        this.authManager = authManager;
        this.jwtUtil     = jwtUtil;
        this.userSvc     = userSvc;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {
        User toCreate = new User();
        toCreate.setUsername(req.getUsername());
        toCreate.setEmail(req.getEmail());
        toCreate.setPassword(req.getPassword());

        try {
            User created = userSvc.register(toCreate);
            UserDto dto = UserDto.from(created);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (DataIntegrityViolationException ex) {
            String causeMsg = ex.getMostSpecificCause().getMessage();
            Map<String, String> errors = new HashMap<>();
            if (causeMsg.contains("email")) {
                errors.put("email", "Email already exists");
            } else if (causeMsg.contains("username")) {
                errors.put("username", "Username already taken");
            } else {
                errors.put("error", "Registration failed due to data integrity violation");
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errors);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    req.getUsername(), 
                    req.getPassword()
                )
            );
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userSvc.findByUsername(req.getUsername())
                          .orElseThrow(() -> 
                              new RuntimeException("User lookup failed after authentication")
                          );

        String token = jwtUtil.generateToken(req.getUsername());

        AuthResponse resp = new AuthResponse(
            token,
            user.getId(),
            user.getEmail()
        );
        return ResponseEntity.ok(resp);
    }
}
