package com.prorenta.financeservice.service.impl.unit_tests;

import com.prorenta.financeservice.exception.CurrencyNotFoundException;
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

import java.util.Optional;
import java.util.UUID;

import static com.prorenta.financeservice.factory.CurrencyDataFactory.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        CurrencyServiceImpl.class
})
public class CurrencyServiceImplUnitTest {

    @Autowired
    private CurrencyService currencyService;

    @MockitoBean
    private CurrencyRepository currencyRepository;

    @Test
    @DisplayName("Поиск валюты по id: успешно")
    public void findCurrencyByIdSuccessfulTest() {
        Currency expected = createDefaultCurrency();

        Mockito.when(currencyRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.ofNullable(expected));

        Currency actual = currencyService.findById(DEFAULT_CURRENCY_ID);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(DEFAULT_CURRENCY_ID, actual.getId());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Поиск валюты по id: валюта не найдена")
    public void findCurrencyByIdCurrencyNotFoundTest() {
        String message = "Валюта с id=" + DEFAULT_CURRENCY_ID + " не найдена";

        Mockito.when(currencyRepository.findById(Mockito.any(UUID.class)))
                .thenThrow(new CurrencyNotFoundException(
                        "Валюта с id=" + DEFAULT_CURRENCY_ID + " не найдена"
                ));

        CurrencyNotFoundException thrown = Assertions.assertThrows(
                CurrencyNotFoundException.class,
                () -> currencyService.findById(DEFAULT_CURRENCY_ID)
        );

        Assertions.assertEquals(message, thrown.getMessage());
    }
}
