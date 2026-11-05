package com.prorenta.financeservice.service.impl.unit_tests.CurrencyRateServiceImplUnitTest;

import com.prorenta.financeservice.exception.CurrencyNotFoundException;
import com.prorenta.financeservice.integration.CbrFeignClient;
import com.prorenta.financeservice.model.dto.ValCursResponseDto;
import com.prorenta.financeservice.model.entity.Currency;
import com.prorenta.financeservice.model.entity.CurrencyRate;
import com.prorenta.financeservice.repository.CurrencyRateRepository;
import com.prorenta.financeservice.service.CurrencyRateService;
import com.prorenta.financeservice.service.CurrencyService;
import com.prorenta.financeservice.service.impl.CurrencyRateServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                CurrencyRateServiceImpl.class
        }
)
public class AggregateRatesCurrencyRateServiceImplUnitTest {

    @Autowired
    private CurrencyRateService currencyRateService;

    @MockitoBean
    private CbrFeignClient cbrFeignClient;

    @MockitoBean
    private CurrencyService currencyService;

    @MockitoBean
    private CurrencyRateRepository currencyRateRepository;

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"31.02.2026", "2026-09-01"})
    @DisplayName("Некорректная дата курса отклоняется")
    void missingOrInvalidDateDoesNotCreateQuotes(String date) {
        // Given
        Mockito.when(cbrFeignClient.getDailyRates(null)).thenReturn(ValCursResponseDto.builder()
                .date(date)
                .currencies(List.of(ValCursResponseDto.Valute.builder()
                        .charCode("USD").nominal(1).value("89,5012").build()))
                .build());

        // When
        currencyRateService.aggregateRates();

        // Then
        Mockito.verifyNoInteractions(currencyRateRepository);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    @DisplayName("Некорректный номинал отклоняется")
    void invalidNominalDoesNotCreateQuote(int nominal) {
        // Given
        Mockito.when(cbrFeignClient.getDailyRates(null)).thenReturn(ValCursResponseDto.builder()
                .date("01.09.2026")
                .currencies(List.of(ValCursResponseDto.Valute.builder()
                        .charCode("USD").nominal(nominal).value("89,5012").build()))
                .build());
        Mockito.when(currencyService.findByCode("USD"))
                .thenReturn(Currency.builder().id(UUID.randomUUID()).code("USD").build());

        // When
        currencyRateService.aggregateRates();

        // Then
        Mockito.verifyNoInteractions(currencyRateRepository);
    }

    @Test
    @DisplayName("Агрегация курсов: создание новой записи курса")
    public void aggregateRatesCreateNewRateSuccessfulTest() {
        // Given
        ValCursResponseDto.Valute usdValute = ValCursResponseDto.Valute.builder()
                .charCode("USD")
                .nominal(1)
                .value("89,5012")
                .build();
        ValCursResponseDto responseDto = ValCursResponseDto.builder()
                .date("01.09.2026")
                .currencies(List.of(usdValute))
                .build();

        UUID usdId = UUID.randomUUID();
        Currency usdCurrency = Currency.builder().id(usdId).code("USD").build();

        Mockito.when(cbrFeignClient.getDailyRates(null))
                .thenReturn(responseDto);
        Mockito.when(currencyService.findByCode("USD"))
                .thenReturn(usdCurrency);
        Mockito.when(currencyRateRepository.findByCurrencyIdAndRateDate(Mockito.eq(usdId), Mockito.eq(LocalDate.of(2026, 9, 1))))
                .thenReturn(Optional.empty());

        // When
        currencyRateService.aggregateRates();

        // Then
        ArgumentCaptor<CurrencyRate> captor = ArgumentCaptor.forClass(CurrencyRate.class);
        Mockito.verify(currencyRateRepository, Mockito.times(1)).save(captor.capture());

        CurrencyRate savedRate = captor.getValue();
        Assertions.assertEquals(usdId, savedRate.getCurrencyId());
        Assertions.assertEquals(LocalDate.of(2026, 9, 1), savedRate.getRateDate());
        Assertions.assertEquals(new BigDecimal("89.5012"), savedRate.getRate());
        Assertions.assertEquals(1, savedRate.getNominal());
    }

    @Test
    @DisplayName("Агрегация курсов: обновление записи")
    public void aggregateRatesUpdateExistingRateSuccessfulTest() {
        // Given
        ValCursResponseDto.Valute usdValute = ValCursResponseDto.Valute.builder()
                .charCode("USD")
                .nominal(1)
                .value("91,2500")
                .build();
        ValCursResponseDto responseDto = ValCursResponseDto.builder()
                .date("01.09.2026")
                .currencies(List.of(usdValute))
                .build();

        UUID usdId = UUID.randomUUID();
        Currency usdCurrency = Currency.builder().id(usdId).code("USD").build();

        CurrencyRate existingRate = CurrencyRate.builder()
                .id(UUID.randomUUID())
                .currencyId(usdId)
                .rateDate(LocalDate.of(2026, 9, 1))
                .rate(new BigDecimal("89.5012"))
                .build();

        Mockito.when(cbrFeignClient.getDailyRates(null))
                .thenReturn(responseDto);
        Mockito.when(currencyService.findByCode("USD"))
                .thenReturn(usdCurrency);
        Mockito.when(currencyRateRepository.findByCurrencyIdAndRateDate(Mockito.eq(usdId), Mockito.eq(LocalDate.of(2026, 9, 1))))
                .thenReturn(Optional.of(existingRate));

        // When
        currencyRateService.aggregateRates();

        // Then
        ArgumentCaptor<CurrencyRate> captor = ArgumentCaptor.forClass(CurrencyRate.class);
        Mockito.verify(currencyRateRepository, Mockito.times(1)).save(captor.capture());

        CurrencyRate updatedRate = captor.getValue();
        Assertions.assertEquals(existingRate.getId(), updatedRate.getId());
        Assertions.assertEquals(new BigDecimal("91.2500"), updatedRate.getRate());
    }

    @Test
    @DisplayName("Агрегация курсов: неизвестная валюта")
    public void aggregateRatesSkipUnknownCurrencyTest() {
        // Given
        ValCursResponseDto.Valute unknownValute = ValCursResponseDto.Valute.builder()
                .charCode("XYZ")
                .nominal(1)
                .value("10,0000")
                .build();
        ValCursResponseDto.Valute validValute = ValCursResponseDto.Valute.builder()
                .charCode("EUR")
                .nominal(1)
                .value("98,2030")
                .build();
        ValCursResponseDto responseDto = ValCursResponseDto.builder()
                .date("01.09.2026")
                .currencies(List.of(unknownValute, validValute))
                .build();

        UUID eurId = UUID.randomUUID();
        Currency eurCurrency = Currency.builder().id(eurId).code("EUR").build();

        Mockito.when(cbrFeignClient.getDailyRates(null))
                .thenReturn(responseDto);
        Mockito.when(currencyService.findByCode("XYZ"))
                .thenThrow(new CurrencyNotFoundException("Валюта с кодом=XYZ не найдена"));
        Mockito.when(currencyService.findByCode("EUR"))
                .thenReturn(eurCurrency);
        Mockito.when(currencyRateRepository.findByCurrencyIdAndRateDate(Mockito.eq(eurId), Mockito.eq(LocalDate.of(2026, 9, 1))))
                .thenReturn(Optional.empty());

        // When
        currencyRateService.aggregateRates();

        // Then
        ArgumentCaptor<CurrencyRate> captor = ArgumentCaptor.forClass(CurrencyRate.class);
        Mockito.verify(currencyRateRepository, Mockito.times(1)).save(captor.capture());

        CurrencyRate savedRate = captor.getValue();
        Assertions.assertEquals(eurId, savedRate.getCurrencyId());
        Assertions.assertEquals(new BigDecimal("98.2030"), savedRate.getRate());
    }

    @Test
    @DisplayName("Агрегация курсов: пустой ответ")
    public void aggregateRatesNullResponseTest() {
        // Given
        Mockito.when(cbrFeignClient.getDailyRates(null))
                .thenReturn(null);

        // When
        currencyRateService.aggregateRates();

        // Then
        Mockito.verifyNoInteractions(currencyService);
        Mockito.verifyNoInteractions(currencyRateRepository);
    }

    @Test
    @DisplayName("Агрегация курсов: сетевая ошибка")
    public void aggregateRatesFeignClientExceptionTest() {
        // Given
        Mockito.when(cbrFeignClient.getDailyRates(null))
                .thenThrow(new RuntimeException("CBR API connection timeout"));

        // When
        Assertions.assertDoesNotThrow(() -> currencyRateService.aggregateRates());

        // Then
        Mockito.verifyNoInteractions(currencyService);
        Mockito.verifyNoInteractions(currencyRateRepository);
    }
}
