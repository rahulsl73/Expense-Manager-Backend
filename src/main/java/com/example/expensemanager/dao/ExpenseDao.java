package com.example.expensemanager.dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.expensemanager.model.Expense;
import com.example.expensemanager.model.User;

public interface ExpenseDao {
    Expense save(Expense expense);
    java.util.Optional<Expense> findById(Long id);
    void deleteById(Long id);

    // Return Page<Expense> so controllers can page & sort
    Page<Expense> findExpenses(User user, String category, LocalDate from, LocalDate to, Pageable pg);

    // Used by BudgetService
    BigDecimal sumAmount(Long userId, LocalDate start, LocalDate end);

    BigDecimal sumByMonth(User user, int year, int month);
    Map<String, BigDecimal> sumByCategory(User user, LocalDate from, LocalDate to);

    BigDecimal getMonthlyTotal(Long userId);
    Long    getMonthlyExpenseCount(Long userId);
    BigDecimal getMonthlyAverage(Long userId);
}
