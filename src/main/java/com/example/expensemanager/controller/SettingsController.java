package com.example.expensemanager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.expensemanager.dto.SettingsDto;
import com.example.expensemanager.mapper.SettingsMapper;
import com.example.expensemanager.service.SettingsService;

@RestController
@RequestMapping("/user/{userId}/settings")
public class SettingsController {

    private final SettingsService service;

    public SettingsController(SettingsService service) {
        this.service = service;
    }

   
    @GetMapping
    public ResponseEntity<SettingsDto> getSettings(
            @PathVariable Long userId) {
        return service.getByUserId(userId)
                .map(SettingsMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<SettingsDto> updateSettings(
            @PathVariable Long userId,
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
