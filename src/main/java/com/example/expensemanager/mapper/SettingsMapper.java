package com.example.expensemanager.mapper;

import com.example.expensemanager.dto.SettingsDto;
import com.example.expensemanager.model.Settings;

public class SettingsMapper {
    public static SettingsDto toDto(Settings s) {
        return SettingsDto.builder()
            .userId(s.getUserId())
            .currencyCode(s.getCurrencyCode())
            .theme(s.getTheme())
            .build();
    }

    public static Settings toEntity(SettingsDto dto) {
        Settings s = new Settings();
        s.setUserId(dto.getUserId());
        s.setCurrencyCode(dto.getCurrencyCode());
        s.setTheme(dto.getTheme());
        return s;
    }
}