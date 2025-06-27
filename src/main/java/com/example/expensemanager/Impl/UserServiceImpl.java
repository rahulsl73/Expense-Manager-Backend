package com.example.expensemanager.Impl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.expensemanager.exception.DuplicateEmailException;
import com.example.expensemanager.exception.DuplicateUsernameException;
import com.example.expensemanager.exception.EntityNotFoundException;
import com.example.expensemanager.model.User;
import com.example.expensemanager.repository.UserRepository;
import com.example.expensemanager.service.UserService;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;
    

    public UserServiceImpl(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public User register(User u) {
        try{
            u.setPassword(encoder.encode(u.getPassword()));
            
            return repo.save(u);
        }catch(DataIntegrityViolationException ex){
            String msg = ex.getMostSpecificCause().getMessage().toLowerCase();
            if (msg.contains("email")) {
                throw new DuplicateEmailException(u.getEmail());
            } else if (msg.contains("username")) {
                throw new DuplicateUsernameException(u.getUsername());
            } else {
                throw ex;  
            }

        }
        
    }

    @Override
    @Transactional(readOnly = true)
    public User  findByIdOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));
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