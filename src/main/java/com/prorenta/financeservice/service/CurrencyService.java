package com.prorenta.financeservice.service;

import com.prorenta.financeservice.model.dto.CurrencyResponseDto;
import com.prorenta.financeservice.model.dto.ListCurrenciesResponseDto;
import com.prorenta.financeservice.model.entity.Currency;

import java.util.UUID;

public interface CurrencyService {
    Currency findById(UUID id);
    CurrencyResponseDto getCurrency(UUID currencyId);
    ListCurrenciesResponseDto getCurrencies();
    Currency findByCode(String code);
}
