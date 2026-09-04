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
import org.springframework.cache.annotation.Cacheable;
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
    @Cacheable(value = "currency_entity_by_id", key = "#id")
    public Currency findById(UUID id) {
        log.info("Поиск валюты: id={}", id);
        return currencyRepository.findById(id).orElseThrow(
                () -> new CurrencyNotFoundException("Валюта с id=" + id + " не найдена")
        );
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "currency_dto_by_id", key = "#currencyId")
    public CurrencyResponseDto getCurrency(UUID currencyId) {
        log.info("Получение валюты: currencyId={}", currencyId);
        Currency currency = currencyRepository.findById(currencyId).orElseThrow(
                () -> new CurrencyNotFoundException("Валюта с id=" + currencyId + " не найдена")
        );
        log.info("Валюта успешно получена: Currency={}", currency);
        return currencyMapper.mapCurrencyToCurrencyResponseDto(currency);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "all_currencies")
    public ListCurrenciesResponseDto getCurrencies() {
        log.info("Получение списка всех валют");
        List<Currency> currencyList = currencyRepository.findAllActiveCurrencies();
        log.info("Список валют получен: size={}", currencyList.size());
        return ListCurrenciesResponseDto.builder()
                .currencies(
                        currencyList.stream()
                                .map(currencyMapper::mapCurrencyToCurrencyResponseDto)
                                .toList()
                )
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "currencies_by_code", key = "#code")
    public Currency findByCode(String code) {
        log.info("Поиск валюты: code={}", code);
        return currencyRepository.findByCode(code)
                .orElseThrow(() -> new CurrencyNotFoundException("Валюта с code=" + code + " не найдена"));
    }
}
