package com.example.expensemanager.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.example.expensemanager.dao.SummaryDao;
import com.example.expensemanager.repository.ExpenseRepository;

@Service
public class SummaryService implements SummaryDao {
    private final ExpenseRepository repo;

    public SummaryService(ExpenseRepository repo) {
        this.repo = repo;
    }

    @Override
    public BigDecimal getTotalSpent(Long userId, LocalDate start, LocalDate end) {
        return repo.sumAmount(userId, start, end);
    }

    @Override
    public Long getExpenseCount(Long userId, LocalDate start, LocalDate end) {
        return repo.countExpenses(userId, start, end);
    }

    @Override
    public BigDecimal getAverageSpent(Long userId, LocalDate start, LocalDate end) {
        Long count = getExpenseCount(userId, start, end);
        if (count == 0) {
            return BigDecimal.ZERO;
        }
        return getTotalSpent(userId, start, end)
            .divide(BigDecimal.valueOf(count), RoundingMode.HALF_UP);
    }
}