package com.prorenta.financeservice.service;

import com.prorenta.financeservice.model.entity.Currency;

import java.util.UUID;

public interface CurrencyService {
    Currency findById(UUID id);
}
