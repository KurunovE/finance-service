package com.prorenta.financeservice.service.impl;

import com.prorenta.financeservice.exception.CurrencyNotFoundException;
import com.prorenta.financeservice.integration.CbrFeignClient;
import com.prorenta.financeservice.model.dto.ValCursResponseDto;
import com.prorenta.financeservice.model.entity.Currency;
import com.prorenta.financeservice.model.entity.CurrencyRate;
import com.prorenta.financeservice.repository.CurrencyRateRepository;
import com.prorenta.financeservice.service.CurrencyRateService;
import com.prorenta.financeservice.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyRateServiceImpl implements CurrencyRateService {

    private final CbrFeignClient cbrFeignClient;
    private final CurrencyService currencyService;
    private final CurrencyRateRepository currencyRateRepository;

    @Override
    @Transactional
    public void aggregateRates() {
        log.info("Запуск планировщика: синхронизация курсов валют");
        try {
            ValCursResponseDto response = cbrFeignClient.getDailyRates(null);

            if (response != null && response.currencies() != null) {
                LocalDate today = LocalDate.now();
                int updatedCount = 0;

                for (ValCursResponseDto.Valute valute : response.currencies()) {
                    String code = valute.charCode();

                    try {
                        Currency currency = currencyService.findByCode(code);
                        UUID currencyId = currency.getId();

                        String normalizedValue = valute.value().replace(",", ".");
                        BigDecimal rate = new BigDecimal(normalizedValue);

                        log.debug("Обработка курса: {} = {} RUB", code, rate);

                        CurrencyRate currencyRate = currencyRateRepository
                                .findByCurrencyIdAndRateDate(currencyId, today)
                                .orElseGet(
                                        () -> CurrencyRate.builder()
                                                .currencyId(currencyId)
                                                .rateDate(today)
                                                .build()
                                );

                        currencyRate.setRate(rate);
                        currencyRateRepository.save(currencyRate);

                        updatedCount++;

                    } catch (CurrencyNotFoundException ex) {
                        log.trace("Валюта {} пропущена, так как отсутствует в справочнике", code);
                    }
                }
                log.info("Успешно агрегировано и сохранено {} курсов валют", updatedCount);
            } else {
                log.warn("API ЦБ РФ вернул пустой ответ или данные недоступны");
            }

        } catch (Exception e) {
            log.error("Ошибка при получении курсов валют: {}", e.getMessage(), e);
        }
    }
}
