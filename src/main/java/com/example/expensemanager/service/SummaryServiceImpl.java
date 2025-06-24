package com.example.expensemanager.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.expensemanager.repository.ExpenseRepository;

@Service
public class SummaryServiceImpl implements SummaryService {
    private final ExpenseRepository repo;

    public SummaryServiceImpl(ExpenseRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalSpent(Long userId, LocalDate start, LocalDate end) {
        return repo.sumAmount(userId, start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getExpenseCount(Long userId, LocalDate start, LocalDate end) {
        return repo.countExpenses(userId, start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAverageSpent(Long userId, LocalDate start, LocalDate end) {
        Long count = getExpenseCount(userId, start, end);
        if (count == 0) {
            return BigDecimal.ZERO;
        }
        return getTotalSpent(userId, start, end)
                .divide(BigDecimal.valueOf(count), RoundingMode.HALF_UP);
    }
}