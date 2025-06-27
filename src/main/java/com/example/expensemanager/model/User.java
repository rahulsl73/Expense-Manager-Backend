package com.example.expensemanager.model;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="users")
public class User {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true, nullable=false)
    
    private String username;

    @Column(nullable=false)
    private String password;

    @Column(unique=true,nullable=false)
    private String email;

    @Column(nullable=false)
    private BigDecimal monthlyBudget = BigDecimal.ZERO;

    @OneToMany(mappedBy="user", cascade=CascadeType.ALL,orphanRemoval=true)
    @JsonManagedReference
    private List<Expense>expenses;

    @Column(nullable = false, length = 64)
    private String jwtSecret;

}
