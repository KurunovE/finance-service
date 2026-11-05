package com.prorenta.financeservice.controller.impl;

import com.prorenta.financeservice.controller.CurrencyController;
import com.prorenta.financeservice.model.dto.CurrencyResponseDto;
import com.prorenta.financeservice.model.dto.ListCurrenciesResponseDto;
import com.prorenta.financeservice.model.dto.ListCurrencyRatesResponseDto;
import com.prorenta.financeservice.service.CurrencyRateService;
import com.prorenta.financeservice.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CurrencyControllerImpl implements CurrencyController {

    private final CurrencyService currencyService;
    private final CurrencyRateService currencyRateService;

    @Override
    public ResponseEntity<ListCurrenciesResponseDto> getCurrencies() {
        log.debug("Запрос на получение всех валют");

        ListCurrenciesResponseDto listCurrenciesResponseDto = currencyService.getCurrencies();

        return ResponseEntity.ok(listCurrenciesResponseDto);
    }

    @Override
    public ResponseEntity<CurrencyResponseDto> getCurrency(UUID currencyId) {
        log.debug("Запрос на получение валюты: currencyId={}", currencyId);

        CurrencyResponseDto currencyResponseDto = currencyService.getCurrency(currencyId);

        return ResponseEntity.ok(currencyResponseDto);
    }

    @Override
    public ResponseEntity<ListCurrencyRatesResponseDto> getCurrencyRates(
            String currencyCode,
            LocalDate startDate,
            LocalDate endDate
    ) {
        log.debug("Запрос на получение курсов валюты {}: startDate={}, endDate={}",
                currencyCode, startDate, endDate);
        ListCurrencyRatesResponseDto listCurrencyRatesResponseDto = currencyRateService.getCurrencyRates(
                currencyCode,
                startDate,
                endDate
        );

        return ResponseEntity.ok(listCurrencyRatesResponseDto);
    }
}
