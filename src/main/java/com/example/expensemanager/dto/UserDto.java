package com.example.expensemanager.dto;

import java.math.BigDecimal;

import com.example.expensemanager.model.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private String username;

    @NotNull @Email
    private String email;

    @NotNull
    private BigDecimal monthlyBudget;
    
    public static UserDto from(User u) {
        return UserDto.builder()
            .username(u.getUsername())
            .email(u.getEmail())
            .monthlyBudget(u.getMonthlyBudget())
            .build();
    }
}
