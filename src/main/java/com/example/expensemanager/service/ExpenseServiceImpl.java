package com.example.expensemanager.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.expensemanager.dao.ExpenseDao;
import com.example.expensemanager.dto.TimeSeriesPoint;
import com.example.expensemanager.model.Expense;
import com.example.expensemanager.model.User;

@Service
public class ExpenseServiceImpl implements ExpenseService {
    private final ExpenseDao dao;

    public ExpenseServiceImpl(ExpenseDao dao) {
        this.dao = dao;
    }

    @Override
    public Optional<Expense> findById(Long id) {
        return dao.findById(id);
    }

    @Override
    @CacheEvict(value = {"monthly", "monthlyCount", "weekly", "daily"}, allEntries = true)
    public Expense create(Expense e) {
        return dao.save(e);
    }

    @Override
    @CacheEvict(value = {"monthly", "monthlyCount", "weekly", "daily"}, allEntries = true)
    public Expense update(Expense e) {
        return dao.save(e);
    }

    @Override
    @CacheEvict(value = {"monthly", "monthlyCount", "weekly", "daily"}, allEntries = true)
    public void delete(Long id) {
        dao.deleteById(id);
    }

    @Override
    public Page<Expense> list(User u, String cat, LocalDate s, LocalDate e, Pageable pg) {
        return dao.findExpenses(u, cat, s, e, pg);
    }

    @Override
    @Cacheable("monthly")
    public BigDecimal total(User u, LocalDate start, LocalDate end) {
        return dao.sumByCategory(u, start, end)
                  .values().stream()
                  .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Cacheable("monthlyCount")
    public Long count(User u, LocalDate start, LocalDate end) {
        return dao.getMonthlyExpenseCount(u.getId());
    }

    @Override
    @Cacheable("SpendByCategoryDonut")
    public Map<String, BigDecimal> getSpendByCategory(User user, LocalDate start, LocalDate end) {
        return dao.sumByCategory(user, start, end);
    }

    @Override
    public List<Expense> findTopByAmount(User user, LocalDate start, LocalDate end, int n) {
        return dao.findExpenses(
                user, null, start, end,
                PageRequest.of(0, n, Sort.by("amount").descending()))
            .getContent();
    }

    @Override
    public List<TimeSeriesPoint> getTimeSeries(User user, LocalDate start, LocalDate end, String interval) {
        Map<LocalDate, BigDecimal> raw;

        switch (interval.toLowerCase()) {
            case "day" -> raw = dao.sumByDay(user, start, end);
            case "week" -> raw = dao.sumByWeek(user, start, end);
            case "month" -> {
                Map<LocalDate, BigDecimal> daily = dao.sumByDay(user, start, end);
                raw = daily.entrySet().stream()
                    .collect(Collectors.toMap(
                        entry -> entry.getKey().withDayOfMonth(1),  
                        Map.Entry::getValue,
                        BigDecimal::add                               
                    ));
            }
            default -> throw new IllegalArgumentException("interval must be day|week|month");
        }

        return raw.entrySet().stream()
            .map(e -> new TimeSeriesPoint(e.getKey(), e.getValue()))
            .sorted(Comparator.comparing(TimeSeriesPoint::getDate))
            .toList();
    }
}