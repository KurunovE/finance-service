package com.prorenta.financeservice.controller.impl;

import com.prorenta.financeservice.controller.CurrencyController;
import com.prorenta.financeservice.model.dto.CurrencyResponseDto;
import com.prorenta.financeservice.model.dto.ListCurrenciesResponseDto;
import com.prorenta.financeservice.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CurrencyControllerImpl implements CurrencyController {

    private final CurrencyService currencyService;

    @Override
    public ResponseEntity<ListCurrenciesResponseDto> getCurrencies() {
        log.debug("Запрос на получения всех валют");
        ListCurrenciesResponseDto listCurrenciesResponseDto = currencyService.getCurrencies();
        return ResponseEntity.ok(listCurrenciesResponseDto);
    }

    @Override
    public ResponseEntity<CurrencyResponseDto> getCurrency(UUID currencyId) {
        log.debug("Запрос на получение валюты: currencyId={}", currencyId);
        CurrencyResponseDto currencyResponseDto = currencyService.getCurrency(currencyId);
        return ResponseEntity.ok(currencyResponseDto);
    }
}
