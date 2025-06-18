package com.example.expensemanager.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.expensemanager.model.Settings;
import com.example.expensemanager.repository.SettingsRepository;

@Service
public class SettingsService {
    private final SettingsRepository repo;

    public SettingsService(SettingsRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public Optional<Settings> getByUserId(Long userId) {
        return repo.findById(userId);
    }

    @Transactional
    public Settings update(Long userId, Settings newSettings) {
        Settings s = repo.findById(userId)
            .orElseGet(() -> {
                newSettings.setUserId(userId);
                return newSettings;
            });
        s.setCurrencyCode(newSettings.getCurrencyCode());
        s.setTheme(newSettings.getTheme());
        return repo.save(s);
    }
}
// do not expose logged out user details