package com.example.expensemanager.Impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.expensemanager.service.ExchangeRateService;

@Service
public class ExchangeRateServiceImpl implements  ExchangeRateService{
    private static final Map<String, BigDecimal> USD_BASED = Map.of(
        "USD", BigDecimal.valueOf(1.00),
        "EUR", BigDecimal.valueOf(0.92),
        "GBP", BigDecimal.valueOf(0.81),
        "INR", BigDecimal.valueOf(83.50),
        "JPY", BigDecimal.valueOf(156.20),
        "CAD", BigDecimal.valueOf(1.35),
        "AUD", BigDecimal.valueOf(1.48)
    );


    @Override
    public BigDecimal getRate(String from, String to) {
        String f = from.toUpperCase();
        String t = to.toUpperCase();

        BigDecimal baseFrom = USD_BASED.get(f);
        BigDecimal baseTo   = USD_BASED.get(t);

        if (baseFrom == null) {
            throw new IllegalArgumentException("Unsupported currency code: " + f);
        }
        if (baseTo == null) {
            throw new IllegalArgumentException("Unsupported currency code: " + t);
        }

        return baseTo.divide(
          baseFrom,
          8,
          RoundingMode.HALF_UP
        );
    }
}
