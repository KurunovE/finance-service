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

    @Test
    @DisplayName("Агрегация курсов: создание новой записи курса")
    public void aggregateRatesCreateNewRateSuccessfulTest() {
        ValCursResponseDto.Valute usdValute = ValCursResponseDto.Valute.builder()
                .charCode("USD")
                .value("89,5012")
                .build();
        ValCursResponseDto responseDto = ValCursResponseDto.builder()
                .currencies(List.of(usdValute))
                .build();

        UUID usdId = UUID.randomUUID();
        Currency usdCurrency = Currency.builder().id(usdId).code("USD").build();

        Mockito.when(cbrFeignClient.getDailyRates(null))
                .thenReturn(responseDto);
        Mockito.when(currencyService.findByCode("USD"))
                .thenReturn(usdCurrency);
        Mockito.when(currencyRateRepository.findByCurrencyIdAndRateDate(Mockito.eq(usdId), Mockito.any(LocalDate.class)))
                .thenReturn(Optional.empty());

        currencyRateService.aggregateRates();

        ArgumentCaptor<CurrencyRate> captor = ArgumentCaptor.forClass(CurrencyRate.class);
        Mockito.verify(currencyRateRepository, Mockito.times(1)).save(captor.capture());

        CurrencyRate savedRate = captor.getValue();
        Assertions.assertEquals(usdId, savedRate.getCurrencyId());
        Assertions.assertEquals(LocalDate.now(), savedRate.getRateDate());
        Assertions.assertEquals(new BigDecimal("89.5012"), savedRate.getRate());
    }

    @Test
    @DisplayName("Агрегация курсов: обновление существующего курса за текущий день")
    public void aggregateRatesUpdateExistingRateSuccessfulTest() {
        ValCursResponseDto.Valute usdValute = ValCursResponseDto.Valute.builder()
                .charCode("USD")
                .value("91,2500")
                .build();
        ValCursResponseDto responseDto = ValCursResponseDto.builder()
                .currencies(List.of(usdValute))
                .build();

        UUID usdId = UUID.randomUUID();
        Currency usdCurrency = Currency.builder().id(usdId).code("USD").build();

        CurrencyRate existingRate = CurrencyRate.builder()
                .id(UUID.randomUUID())
                .currencyId(usdId)
                .rateDate(LocalDate.now())
                .rate(new BigDecimal("89.5012"))
                .build();

        Mockito.when(cbrFeignClient.getDailyRates(null))
                .thenReturn(responseDto);
        Mockito.when(currencyService.findByCode("USD"))
                .thenReturn(usdCurrency);
        Mockito.when(currencyRateRepository.findByCurrencyIdAndRateDate(Mockito.eq(usdId), Mockito.any(LocalDate.class)))
                .thenReturn(Optional.of(existingRate));

        currencyRateService.aggregateRates();

        ArgumentCaptor<CurrencyRate> captor = ArgumentCaptor.forClass(CurrencyRate.class);
        Mockito.verify(currencyRateRepository, Mockito.times(1)).save(captor.capture());

        CurrencyRate updatedRate = captor.getValue();
        Assertions.assertEquals(existingRate.getId(), updatedRate.getId());
        Assertions.assertEquals(new BigDecimal("91.2500"), updatedRate.getRate());
    }

    @Test
    @DisplayName("Агрегация курсов: пропуск неизвестных валют без прерывания цикла")
    public void aggregateRatesSkipUnknownCurrencyTest() {
        ValCursResponseDto.Valute unknownValute = ValCursResponseDto.Valute.builder()
                .charCode("XYZ")
                .value("10,0000")
                .build();
        ValCursResponseDto.Valute validValute = ValCursResponseDto.Valute.builder()
                .charCode("EUR")
                .value("98,2030")
                .build();
        ValCursResponseDto responseDto = ValCursResponseDto.builder()
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
        Mockito.when(currencyRateRepository.findByCurrencyIdAndRateDate(Mockito.eq(eurId), Mockito.any(LocalDate.class)))
                .thenReturn(Optional.empty());

        currencyRateService.aggregateRates();

        ArgumentCaptor<CurrencyRate> captor = ArgumentCaptor.forClass(CurrencyRate.class);
        Mockito.verify(currencyRateRepository, Mockito.times(1)).save(captor.capture());

        CurrencyRate savedRate = captor.getValue();
        Assertions.assertEquals(eurId, savedRate.getCurrencyId());
        Assertions.assertEquals(new BigDecimal("98.2030"), savedRate.getRate());
    }

    @Test
    @DisplayName("Агрегация курсов: безопасное завершение при null от клиента")
    public void aggregateRatesNullResponseTest() {
        Mockito.when(cbrFeignClient.getDailyRates(null)).thenReturn(null);

        currencyRateService.aggregateRates();

        Mockito.verifyNoInteractions(currencyService);
        Mockito.verifyNoInteractions(currencyRateRepository);
    }

    @Test
    @DisplayName("Агрегация курсов: перехват сетевой ошибки от Feign-клиента")
    public void aggregateRatesFeignClientExceptionTest() {
        Mockito.when(cbrFeignClient.getDailyRates(null))
                .thenThrow(new RuntimeException("CBR API connection timeout"));
        Assertions.assertDoesNotThrow(() -> currencyRateService.aggregateRates());

        Mockito.verifyNoInteractions(currencyService);
        Mockito.verifyNoInteractions(currencyRateRepository);
    }
}