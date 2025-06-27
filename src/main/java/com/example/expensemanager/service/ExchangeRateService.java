package com.example.expensemanager.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

@Service
public interface ExchangeRateService {
    BigDecimal getRate(String from,String to);
}
