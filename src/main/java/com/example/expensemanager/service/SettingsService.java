package com.example.expensemanager.service;

import java.util.Optional;

import com.example.expensemanager.model.Settings;

public interface SettingsService {

    Optional<Settings> getByUserId(Long userId);

    Settings update(Long userId, Settings newSettings);
}