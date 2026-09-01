package com.prorenta.financeservice.service.impl.unit_tests.CurrencyServiceImplUnitTest;

import com.prorenta.financeservice.exception.CurrencyNotFoundException;
import com.prorenta.financeservice.mapper.CurrencyMapperImpl;
import com.prorenta.financeservice.model.dto.CurrencyResponseDto;
import com.prorenta.financeservice.model.dto.ListCurrenciesResponseDto;
import com.prorenta.financeservice.model.entity.Currency;
import com.prorenta.financeservice.repository.CurrencyRepository;
import com.prorenta.financeservice.service.CurrencyService;
import com.prorenta.financeservice.service.impl.CurrencyServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static com.prorenta.financeservice.factory.CurrencyDataFactory.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                CurrencyServiceImpl.class,
                CurrencyMapperImpl.class
        }
)
public class GetCurrencyServiceImplUnitTest {

    @Autowired
    private CurrencyService currencyService;

    @MockitoBean
    private CurrencyRepository currencyRepository;

    @Test
    @DisplayName("Получение валюты по ID: успешно")
    public void getCurrencySuccessfulTest() {
        Currency currency = createDefaultCurrency();
        Mockito.when(currencyRepository.findById(DEFAULT_CURRENCY_ID))
                .thenReturn(Optional.of(currency));

        CurrencyResponseDto actual = currencyService.getCurrency(DEFAULT_CURRENCY_ID);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(currency.getId(), actual.id());
        Assertions.assertEquals(currency.getCode(), actual.code());
        Assertions.assertEquals(currency.getName(), actual.name());
    }

    @Test
    @DisplayName("Получение валюты по ID: валюта не найдена")
    public void getCurrencyNotFoundTest() {
        String expectedMessage = "Валюта с id=" + DEFAULT_CURRENCY_ID + " не найдена";
        Mockito.when(currencyRepository.findById(DEFAULT_CURRENCY_ID))
                .thenReturn(Optional.empty());

        CurrencyNotFoundException thrown = Assertions.assertThrows(
                CurrencyNotFoundException.class,
                () -> currencyService.getCurrency(DEFAULT_CURRENCY_ID)
        );

        Assertions.assertEquals(expectedMessage, thrown.getMessage());
    }

    @Test
    @DisplayName("Получение списка валют: успешно (список не пуст)")
    public void getCurrenciesSuccessfulTest() {
        Currency currency = createDefaultCurrency();
        Mockito.when(currencyRepository.findAllActiveCurrencies())
                .thenReturn(List.of(currency));

        ListCurrenciesResponseDto actual = currencyService.getCurrencies();

        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.currencies());
        Assertions.assertEquals(1, actual.currencies().size());
        Assertions.assertEquals(currency.getId(), actual.currencies().getFirst().id());
    }

    @Test
    @DisplayName("Получение списка валют: пустой список")
    public void getCurrenciesEmptyListTest() {
        Mockito.when(currencyRepository.findAllActiveCurrencies())
                .thenReturn(List.of());

        ListCurrenciesResponseDto actual = currencyService.getCurrencies();

        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.currencies());
        Assertions.assertTrue(actual.currencies().isEmpty());
    }
}
