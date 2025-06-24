package com.example.expensemanager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.expensemanager.dto.SettingsDto;
import com.example.expensemanager.mapper.SettingsMapper;
import com.example.expensemanager.service.SettingsService;

@RestController
@RequestMapping("/settings")
public class SettingsController {

    private final SettingsService service;

    public SettingsController(SettingsService service) {
        this.service = service;
    }

    /**
     * Get current user's settings. Relies solely on User-Id header.
     */
    @GetMapping
    public ResponseEntity<SettingsDto> getSettings(
            @RequestHeader("User-Id") Long userId) {
        return service.getByUserId(userId)
                .map(SettingsMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update current user's settings. Only User-Id header is required.
     */
    @PutMapping
    public ResponseEntity<SettingsDto> updateSettings(
            @RequestHeader("User-Id") Long userId,
            @RequestBody SettingsDto dto) {

        // Validate required fields
        if (dto.getCurrencyCode() == null || dto.getCurrencyCode().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (dto.getTheme() == null) {
            return ResponseEntity.badRequest().build();
        }

        // Override any client-supplied userId
        dto.setUserId(userId);

        var updated = service.update(userId, SettingsMapper.toEntity(dto));
        return ResponseEntity.ok(SettingsMapper.toDto(updated));
    }
}
