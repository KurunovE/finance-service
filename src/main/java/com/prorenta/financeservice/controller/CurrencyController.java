package com.prorenta.financeservice.controller;

import com.prorenta.financeservice.model.dto.CurrencyResponseDto;
import com.prorenta.financeservice.model.dto.ListCurrenciesResponseDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Tag(name = "Currencies", description = "Операции с валютами")
@RequestMapping("api/v1/currencies")
public interface CurrencyController {

    @GetMapping
    ResponseEntity<ListCurrenciesResponseDto> getCurrencies();

    @GetMapping("/{currencyId}")
    ResponseEntity<CurrencyResponseDto> getCurrency(
            @PathVariable UUID currencyId
    );
}
