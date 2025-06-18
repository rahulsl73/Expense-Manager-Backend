package com.example.expensemanager.dao;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface SummaryDao {
    BigDecimal getTotalSpent(Long userId, LocalDate start, LocalDate end);
    Long getExpenseCount(Long userId, LocalDate start, LocalDate end);
    BigDecimal getAverageSpent(Long userId, LocalDate start, LocalDate end);
}
