package com.example.expensemanager.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.expensemanager.model.Settings;

public interface SettingsRepository extends JpaRepository<Settings, Long> {
    // no additional methods needed
}