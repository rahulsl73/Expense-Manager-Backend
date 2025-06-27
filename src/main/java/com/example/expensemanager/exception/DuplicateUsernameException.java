package com.example.expensemanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException(String username) {
        super("Username already exists: " + username);
    }
}

