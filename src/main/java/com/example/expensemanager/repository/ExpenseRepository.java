package com.example.expensemanager.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.expensemanager.model.Expense;
import com.example.expensemanager.model.User;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Page<Expense> findByUserAndCategoryAndDateBetween(
        User user,
        String category,
        LocalDate start,
        LocalDate end,
        Pageable pageable
    );

    Page<Expense> findByUserAndDateBetween(
        User user,
        LocalDate start,
        LocalDate end,
        Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(e.amount),0) " +
           "FROM Expense e " +
           "WHERE e.user.id = :uid " +
           "  AND e.date BETWEEN :start AND :end")
    BigDecimal sumAmount(
        @Param("uid") Long uid,
        @Param("start") LocalDate start,
        @Param("end")   LocalDate end
    );

    @Query("SELECT COUNT(e) " +
           "FROM Expense e " +
           "WHERE e.user.id = :uid " +
           "  AND e.date BETWEEN :start AND :end")
    Long countExpenses(
        @Param("uid") Long uid,
        @Param("start") LocalDate start,
        @Param("end")   LocalDate end
    );

    // Category-wise sums for breakdown/charting
    @Query("SELECT e.category, COALESCE(SUM(e.amount), 0) " +
           "FROM Expense e " +
           "WHERE e.user.id = :uid " +
           "  AND e.date BETWEEN :start AND :end " +
           "GROUP BY e.category")
    List<Object[]> sumByCategory(
        @Param("uid") Long uid,
        @Param("start") LocalDate start,
        @Param("end")   LocalDate end
    );

    @Query("""
      SELECT e.date AS d, COALESCE(SUM(e.amount),0)
      FROM Expense e
      WHERE e.user.id = :uid AND e.date BETWEEN :start AND :end
      GROUP BY e.date
      ORDER BY e.date
    """)
    List<Object[]> sumByDay(
      @Param("uid")   Long uid,
      @Param("start") LocalDate start,
      @Param("end")   LocalDate end
    );

  
    @Query(value = """
      SELECT DATE_TRUNC('week', e.date)::date AS week_start,
            COALESCE(SUM(e.amount),0)
      FROM expenses e
      WHERE e.user_id = :uid
        AND e.date BETWEEN :start AND :end
      GROUP BY week_start
      ORDER BY week_start
      """, nativeQuery = true)
    List<Object[]> sumByWeek(
      @Param("uid")   Long uid,
      @Param("start") LocalDate start,
      @Param("end")   LocalDate end
    );


    @Query(value = """
    SELECT DATE_TRUNC('month', e.date)::date AS month_start, 
          COALESCE(SUM(e.amount), 0)
    FROM expenses e
    WHERE e.user_id = :uid
      AND e.date BETWEEN :start AND :end
    GROUP BY month_start
    ORDER BY month_start
    """, nativeQuery = true)
    List<Object[]> sumByMonthGrouped(
        @Param("uid") Long uid,
        @Param("start") LocalDate start,
        @Param("end")   LocalDate end
    );

}
