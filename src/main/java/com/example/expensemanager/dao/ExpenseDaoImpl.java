package com.example.expensemanager.dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.expensemanager.model.Expense;
import com.example.expensemanager.model.User;
import com.example.expensemanager.repository.ExpenseRepository;

@Repository
public class ExpenseDaoImpl implements ExpenseDao {
    private final ExpenseRepository repo;

    public ExpenseDaoImpl(ExpenseRepository repo) {
        this.repo = repo;
    }

    @Override
    public Expense save(Expense expense) {
        return repo.save(expense);
    }

    @Override
    public java.util.Optional<Expense> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    @Override
    public Page<Expense> findExpenses(
            User user, String category, LocalDate from, LocalDate to, Pageable pg) {
        if (category != null && !category.isBlank()) {
            return repo.findByUserAndCategoryAndDateBetween(user, category, from, to, pg);
        }
        return repo.findByUserAndDateBetween(user, from, to, pg);
    }

    @Override
    public BigDecimal sumAmount(Long userId, LocalDate start, LocalDate end) {
        return repo.sumAmount(userId, start, end);
    }

    // @Override
    // public BigDecimal sumByMonth(User user, int year, int month) {
    //     LocalDate first = LocalDate.of(year, month, 1);
    //     LocalDate last  = first.with(TemporalAdjusters.lastDayOfMonth());
    //     return repo.sumAmount(user.getId(), first, last);
    // }

    @Override
    public Map<String, BigDecimal> sumByCategory(User user, LocalDate from, LocalDate to) {
        List<Object[]> raw = repo.sumByCategory(user.getId(), from, to);
        return raw.stream().collect(Collectors.toMap(
            r -> (String)   r[0],
            r -> (BigDecimal) r[1]
        ));
    }

    // @Override
    // public BigDecimal getMonthlyTotal(Long userId) {
    //     LocalDate now = LocalDate.now();
    //     return repo.sumAmount(userId, now.withDayOfMonth(1), now);
    // }

    @Override
    public Long getMonthlyExpenseCount(Long userId) {
        LocalDate now = LocalDate.now();
        return repo.countExpenses(userId, now.withDayOfMonth(1), now);
    }

    // @Override
    // public BigDecimal getMonthlyAverage(Long userId) {
    //     Long count = getMonthlyExpenseCount(userId);
    //     if (count == 0) return BigDecimal.ZERO;
    //     return getMonthlyTotal(userId)
    //         .divide(BigDecimal.valueOf(count), RoundingMode.HALF_UP);
    // }

    @Override
    public Map<LocalDate, BigDecimal> sumByDay(User user, LocalDate start, LocalDate end) {
        List<Object[]> raw = repo.sumByDay(user.getId(), start, end);
        return raw.stream().collect(Collectors.toMap(
            r -> (LocalDate)   r[0],
            r -> (BigDecimal)  r[1]
        ));
    }

   @Override
    public Map<LocalDate, BigDecimal> sumByWeek(User user, LocalDate start, LocalDate end) {
        List<Object[]> raw = repo.sumByWeek(user.getId(), start, end);
        return raw.stream().collect(Collectors.toMap(
            entry -> {
                Object val = entry[0];
                LocalDate weekStart;
                if (val instanceof java.sql.Date) {
                    weekStart = ((java.sql.Date) val).toLocalDate();
                } else if (val instanceof java.time.Instant) {
                    weekStart = ((java.time.Instant) val)
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDate();
                } else if (val instanceof java.sql.Timestamp) {
                    weekStart = ((java.sql.Timestamp) val)
                                    .toLocalDateTime()
                                    .toLocalDate();
                } else {
                    throw new IllegalStateException("Unexpected type for week_start: " + val.getClass());
                }
                // Normalize to Monday
                return weekStart.with(java.time.DayOfWeek.MONDAY);
            },
            entry -> (BigDecimal) entry[1]
        ));
    }


    // @Override
    // public Map<LocalDate, BigDecimal> sumByMonthGrouped(User user, LocalDate start, LocalDate end) {
    //     List<Object[]> raw = repo.sumByMonthGrouped(user.getId(), start, end);
    //     return raw.stream().collect(Collectors.toMap(
    //         entry -> {
    //             Object val = entry[0];
    //             LocalDate date;
    //             if (val instanceof java.time.Instant) {
    //                 date = ((java.time.Instant) val)
    //                         .atZone(java.time.ZoneId.systemDefault())
    //                         .toLocalDate();
    //             } else if (val instanceof java.sql.Timestamp) {
    //                 date = ((java.sql.Timestamp) val)
    //                         .toLocalDateTime().toLocalDate();
    //             } else {
    //                 date = ((java.sql.Date) val).toLocalDate();
    //             }
    //             return date.withDayOfMonth(1);
    //         },
    //         entry -> (BigDecimal) entry[1]
    //     ));
    // }


}
