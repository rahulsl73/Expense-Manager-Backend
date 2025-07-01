package com.example.expensemanager.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name ="settings")
public class Settings {
    @Id
    private Long userId;

    @Column(length = 3, nullable = false)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Theme theme;
}