package com.example.expensemanager.Impl;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.expensemanager.model.User;
import com.example.expensemanager.repository.UserRepository;
import com.example.expensemanager.service.UserService;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    public UserServiceImpl(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public User register(User u) {
        u.setPassword(encoder.encode(u.getPassword()));
        byte[] keyBytes = new byte[32];
        SECURE_RANDOM.nextBytes(keyBytes);
        u.setJwtSecret(HEX_FORMAT.formatHex(keyBytes));
        return repo.save(u);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return repo.findByUsername(username);
    }

    @Override
    @Transactional
    public User updateProfile(Long uid, String newEmail, BigDecimal newBudget) {
        User u = repo.findById(uid)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + uid));
        u.setEmail(newEmail);
        u.setMonthlyBudget(newBudget);
        return repo.save(u);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repo.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
            .password(user.getPassword())
            .authorities(Collections.emptyList())
            .build();
    }
}