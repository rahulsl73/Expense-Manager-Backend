package com.example.expensemanager.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.expensemanager.model.Settings;
import com.example.expensemanager.service.SettingsService;


@RestController
@RequestMapping("/settings")
public class SettingsController {
    private final SettingsService service;

    public SettingsController(SettingsService service) {
        this.service = service;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Settings> getSettings(
            @PathVariable Long userId,
            @RequestHeader("User-Id") Long headerUid) {
        if (!headerUid.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return service.getByUserId(userId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{userId}")
    public ResponseEntity<Settings> updateSettings(
            @PathVariable Long userId,
            @RequestHeader("User-Id") Long headerUid,
            @RequestBody Settings settings) {
        if (!headerUid.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Settings updated = service.update(userId, settings);
        return ResponseEntity.ok(updated);
    }
}
