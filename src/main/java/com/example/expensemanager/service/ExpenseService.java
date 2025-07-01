package com.example.expensemanager.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.expensemanager.dto.TimeSeriesPoint;
import com.example.expensemanager.model.Expense;
import com.example.expensemanager.model.User;

public interface ExpenseService {

    Optional<Expense> findById(Long id);

    @CacheEvict(value = {"monthly", "monthlyCount", "weekly", "daily"}, allEntries = true)
    Expense create(Expense e);

    @CacheEvict(value = {"monthly", "monthlyCount", "weekly", "daily"}, allEntries = true)
    Expense update(Expense e);

    @CacheEvict(value = {"monthly", "monthlyCount", "weekly", "daily"}, allEntries = true)
    void delete(Long id);

    Page<Expense> list(User u, String cat, LocalDate s, LocalDate e, Pageable pg);

    @Cacheable("monthly")
    BigDecimal total(User u, LocalDate start, LocalDate end);

    @Cacheable("monthlyCount")
    Long count(User u, LocalDate start, LocalDate end);

    @Cacheable("SpendByCategoryDonut")
    Map<String, BigDecimal> getSpendByCategory(User user, LocalDate start, LocalDate end);

    List<Expense> findTopByAmount(User user, LocalDate start, LocalDate end, int n);

    List<TimeSeriesPoint> getTimeSeries(User user, LocalDate start, LocalDate end, String interval);
}
