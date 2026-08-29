package com.prorenta.financeservice.service.impl;

import com.prorenta.financeservice.exception.CurrencyNotFoundException;
import com.prorenta.financeservice.mapper.CurrencyMapper;
import com.prorenta.financeservice.model.dto.CurrencyResponseDto;
import com.prorenta.financeservice.model.dto.ListCurrenciesResponseDto;
import com.prorenta.financeservice.model.entity.Currency;
import com.prorenta.financeservice.repository.CurrencyRepository;
import com.prorenta.financeservice.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final CurrencyMapper currencyMapper;

    @Override
    @Transactional(readOnly = true)
    public Currency findById(UUID id) {
        return currencyRepository.findById(id).orElseThrow(
                () -> new CurrencyNotFoundException("Валюта с id=" + id + " не найдена")
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CurrencyResponseDto getCurrency(UUID currencyId) {
        log.info("Получение валюты: currencyId={}", currencyId);
        Currency currency = findById(currencyId);
        log.info("Валюта успешно получена: Currency={}", currency);
        return currencyMapper.mapCurrencyToCurrencyResponseDto(currency);
    }

    @Override
    @Transactional(readOnly = true)
    public ListCurrenciesResponseDto getCurrencies() {
        log.info("Получение списка всех валют");
        List<Currency> currencyList = currencyRepository.findAll();
        log.info("Список валют получен: size={}", currencyList.size());
        return ListCurrenciesResponseDto.builder()
                .currencies(
                        currencyList.stream()
                                .map(currencyMapper::mapCurrencyToCurrencyResponseDto)
                                .toList()
                )
                .build();
    }
}
