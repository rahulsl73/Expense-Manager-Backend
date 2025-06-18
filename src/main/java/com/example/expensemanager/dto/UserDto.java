package com.example.expensemanager.dto;

import com.example.expensemanager.model.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a user, excluding sensitive data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String email;

    public static UserDto from(User u) {
        return UserDto.builder()
            .id(u.getId())
            .username(u.getUsername())
            .email(u.getEmail())
            .build();
    }
}
