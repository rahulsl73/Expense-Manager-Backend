package com.example.expensemanager.service;

import com.example.expensemanager.dao.ExpenseDao;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.example.expensemanager.model.User;

@Service
public class BudgetService {
    private final ExpenseDao expenseDao;

    public BudgetService(ExpenseDao expenseDao) {
        this.expenseDao = expenseDao;
    }

    public BigDecimal getMonthlyTotal(User user) {
        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        return expenseDao.sumAmount(user.getId(), start, now);
    }

    public boolean isOverBudget(User user, BigDecimal newExpense) {
        BigDecimal total = getMonthlyTotal(user);
        return total.add(newExpense).compareTo(user.getMonthlyBudget()) > 0;
    }
}

