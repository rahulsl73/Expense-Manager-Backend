package com.example.expensemanager.dto;

import com.example.expensemanager.model.Theme;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsDto {
    @NotNull
    private Long userId;

    @NotBlank
    private String currencyCode;

    @NotNull
    private Theme theme;  
}