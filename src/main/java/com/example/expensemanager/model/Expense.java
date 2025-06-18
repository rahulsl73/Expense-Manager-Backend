package com.example.expensemanager.model;

import jakarta.persistence.*;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name ="expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(nullable =false)
    private String title;

    @Column(nullable=false)
    private BigDecimal amount;

    @Column(nullable =false)
    private String category;

    @Column(nullable=false)
    private LocalDate date;

    @ElementCollection
    private List<String>tags;

    @Column(length = 1000)
    private String note;

    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name ="user_id", nullable =false)
    private User user;

}
