package com.prorenta.financeservice.service.impl;

import com.prorenta.financeservice.exception.CurrencyNotFoundException;
import com.prorenta.financeservice.model.entity.Currency;
import com.prorenta.financeservice.repository.CurrencyRepository;
import com.prorenta.financeservice.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;

    @Override
    public Currency findById(UUID id) {
        return currencyRepository.findById(id).orElseThrow(
                () -> new CurrencyNotFoundException("Валюта с id=" + id + " не найдена")
        );
    }
}
