package com.example.expensemanager.dao;

import java.util.Optional;

import com.example.expensemanager.model.User;

public interface UserDao {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    User save(User user);
}
