package com.example.expensemanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TimeSeriesPoint(LocalDate date, BigDecimal total) {
   
    public LocalDate getDate() {
        return date;
    }
    public BigDecimal getTotal() {
        return total;
    }
}

