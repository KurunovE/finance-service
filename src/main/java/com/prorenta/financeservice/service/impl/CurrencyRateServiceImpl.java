package com.prorenta.financeservice.service.impl;

import com.prorenta.financeservice.exception.CurrencyNotFoundException;
import com.prorenta.financeservice.exception.LimitExceededException;
import com.prorenta.financeservice.integration.CbrFeignClient;
import com.prorenta.financeservice.model.dto.CurrencyRateDto;
import com.prorenta.financeservice.model.dto.ListCurrencyRatesResponseDto;
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
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyRateServiceImpl implements CurrencyRateService {

    private static final int YEARS_LIMIT = 5;
    private static final DateTimeFormatter CBR_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final CbrFeignClient cbrFeignClient;
    private final CurrencyService currencyService;
    private final CurrencyRateRepository currencyRateRepository;

    @Override
    @Transactional
    public void aggregateRates() {
        log.debug("Начало синхронизации курсов валют");

        try {
            ValCursResponseDto response = cbrFeignClient.getDailyRates(null);

            if (response != null && response.currencies() != null) {
                LocalDate rateDate = LocalDate.parse(response.date(), CBR_DATE_FORMAT);
                int updatedCount = 0;

                for (ValCursResponseDto.Valute valute : response.currencies()) {
                    String code = valute.charCode();

                    try {
                        Currency currency = currencyService.findByCode(code);
                        UUID currencyId = currency.getId();

                        String normalizedValue = valute.value().replace(",", ".");
                        BigDecimal rate = new BigDecimal(normalizedValue);
                        Integer nominal = valute.nominal();

                        if (nominal == null || nominal <= 0) {
                            throw new IllegalArgumentException("Некорректный номинал валюты " + code);
                        }

                        log.debug("Обработка курса: {} {} = {} RUB, дата={}", nominal, code, rate, rateDate);

                        CurrencyRate currencyRate = currencyRateRepository
                                .findByCurrencyIdAndRateDate(currencyId, rateDate)
                                .orElseGet(
                                        () -> CurrencyRate.builder()
                                                .currencyId(currencyId)
                                                .rateDate(rateDate)
                                                .build()
                                );

                        currencyRate.setRate(rate);
                        currencyRate.setNominal(nominal);
                        currencyRateRepository.save(currencyRate);

                        updatedCount++;

                    } catch (CurrencyNotFoundException ex) {
                        log.trace("Валюта {} пропущена, так как отсутствует в справочнике", code);
                    }
                }

                log.info("Успешно агрегировано и сохранено {} курсов валют", updatedCount);
            } else {
                log.warn("Пустой ответ или данные недоступны");
            }

        } catch (Exception e) {
            log.error("Ошибка при получении курсов валют: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ListCurrencyRatesResponseDto getCurrencyRates(
            String currencyCode,
            LocalDate startDate,
            LocalDate endDate
    ) {
        log.debug("Получение списка курсов валюты {} за период c {} по {}",
                currencyCode, startDate, endDate);

        if (endDate.isBefore(startDate)) {
            throw new LimitExceededException("Дата окончания не может быть раньше даты начала");
        }

        if (startDate.plusYears(YEARS_LIMIT).isBefore(endDate)) {
            throw new LimitExceededException("Запрашиваемый период для графика не может превышать " + YEARS_LIMIT + " лет");
        }

        Currency currency = currencyService.findByCode(currencyCode);

        log.debug("Валюта найдена: currencyCode={}", currencyCode);

        List<CurrencyRateDto> currencyResponseDtoList = currencyRateRepository.findAllByCurrencyIdAndRateDateBetweenOrderByRateDateAsc(
                currency.getId(),
                startDate,
                endDate
        );

        log.debug("Список курсов валют загружен: size={}", currencyResponseDtoList.size());

        return ListCurrencyRatesResponseDto.builder()
                .rates(currencyResponseDtoList)
                .build();
    }
}
