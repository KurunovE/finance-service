package com.prorenta.financeservice.service.impl.integration_tests.CurrencyServiceImplIntegrationTest;

import com.prorenta.financeservice.repository.CurrencyRepository;
import com.prorenta.financeservice.service.CurrencyService;
import com.prorenta.financeservice.service.impl.integration_tests.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;

import java.util.Objects;
import java.util.UUID;

public class GetCurrencyServiceImplCacheIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CurrencyService currencyService;

    @MockitoSpyBean
    private CurrencyRepository currencyRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    public void setUpCache() {
        cacheManager.getCacheNames().forEach(
                cacheName -> Objects.requireNonNull(cacheManager.getCache(cacheName)).clear()
        );
        Mockito.reset(currencyRepository);
    }

    @Test
    @Sql(
            scripts = {
                    "/sql/cleanup.sql",
                    "/sql/insert_currency.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @DisplayName("Получение валюты: кэширование списка валют (all_currencies)")
    public void getCurrenciesCacheTest() {
        currencyService.getCurrencies();
        currencyService.getCurrencies();
        currencyService.getCurrencies();

        Mockito.verify(currencyRepository, Mockito.times(1))
                .findAllActiveCurrencies();
    }

    @Test
    @Sql(
            scripts = {
                    "/sql/cleanup.sql",
                    "/sql/insert_currency.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @DisplayName("Получение валюты: кэширование валюты по ID (currency_dto_by_id)")
    public void getCurrencyByIdCacheTest() {
        UUID currencyId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        currencyService.getCurrency(currencyId);
        currencyService.getCurrency(currencyId);
        currencyService.getCurrency(currencyId);

        Mockito.verify(currencyRepository, Mockito.times(1))
                .findById(currencyId);
    }

    @Test
    @Sql(
            scripts = {
                    "/sql/cleanup.sql",
                    "/sql/insert_currency.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @DisplayName("Получение валюты: кэширование валюты по коду (currencies_by_code)")
    public void getCurrencyByCodeCacheTest() {
        String code = "RUB";

        currencyService.findByCode(code);
        currencyService.findByCode(code);
        currencyService.findByCode(code);

        Mockito.verify(currencyRepository, Mockito.times(1))
                .findByCode(code);
    }
}
