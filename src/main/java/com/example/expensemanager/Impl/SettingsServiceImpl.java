package com.example.expensemanager.Impl;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.expensemanager.model.Settings;
import com.example.expensemanager.repository.SettingsRepository;
import com.example.expensemanager.service.SettingsService;

@Service
public class SettingsServiceImpl implements SettingsService {
    private final SettingsRepository repo;

    public SettingsServiceImpl(SettingsRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Settings> getByUserId(Long userId) {
        return repo.findById(userId);
    }

    @Override
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