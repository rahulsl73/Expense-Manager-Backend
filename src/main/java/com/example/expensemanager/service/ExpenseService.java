// src/main/java/com/example/expensemanager/service/ExpenseService.java
package com.example.expensemanager.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.expensemanager.dao.ExpenseDao;
import com.example.expensemanager.model.Expense;
import com.example.expensemanager.model.User;

@Service
public class ExpenseService {
    private final ExpenseDao dao;

    public ExpenseService(ExpenseDao dao) {
        this.dao = dao;
    }

    public Optional<Expense> findById(Long id) {
        return dao.findById(id);
    }

    @CacheEvict(value={"monthly","monthlyCount","weekly","daily"}, allEntries=true)
    public Expense create(Expense e) {
        return dao.save(e);
    }

    @CacheEvict(value={"monthly","monthlyCount","weekly","daily"}, allEntries=true)
    public Expense update(Expense e) {
        return dao.save(e);
    }

    @CacheEvict(value={"monthly","monthlyCount","weekly","daily"}, allEntries=true)
    public void delete(Long id) {
        dao.deleteById(id);
    }

    public Page<Expense> list(User u, String cat, LocalDate s, LocalDate e, Pageable pg) {
        return dao.findExpenses(u, cat, s, e, pg);
    }

    @Cacheable("monthly")
    public BigDecimal total(User u, LocalDate start, LocalDate end) {
        return dao.sumByCategory(u, start, end)
                  .values().stream()
                  .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Cacheable("monthlyCount")
    public Long count(User u, LocalDate start, LocalDate end) {
        return dao.getMonthlyExpenseCount(u.getId());
    }
}
