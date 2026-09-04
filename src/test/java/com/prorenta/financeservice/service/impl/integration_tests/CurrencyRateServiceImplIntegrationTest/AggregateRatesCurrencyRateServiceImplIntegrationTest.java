package com.prorenta.financeservice.service.impl.integration_tests.CurrencyRateServiceImplIntegrationTest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.prorenta.financeservice.factory.CbrApiDataFactory;
import com.prorenta.financeservice.model.entity.CurrencyRate;
import com.prorenta.financeservice.repository.CurrencyRateRepository;
import com.prorenta.financeservice.service.CurrencyRateService;
import com.prorenta.financeservice.service.impl.integration_tests.AbstractIntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

public class AggregateRatesCurrencyRateServiceImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CurrencyRateService currencyRateService;

    @Autowired
    private CurrencyRateRepository currencyRateRepository;

    @Test
    @Sql(
            scripts = {
                    "/sql/cleanup.sql",
                    "/sql/insert_currency.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @DisplayName("Интеграционный тест: Успешная агрегация курсов валют (парсинг XML от ЦБ РФ)")
    public void aggregateRatesSuccessfully() {
        String xmlResponse = CbrApiDataFactory.cbrFeignClientResponse;

        WireMock.stubFor(get(urlPathEqualTo("/XML_daily.asp"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml; charset=windows-1251")
                        .withBody(xmlResponse)));

        currencyRateService.aggregateRates();

        List<CurrencyRate> savedRates = currencyRateRepository.findAll();

        Assertions.assertThat(savedRates).hasSize(1);
        CurrencyRate usdRate = savedRates.getFirst();
        Assertions.assertThat(usdRate.getRate()).isEqualByComparingTo("89.5012");
        Assertions.assertThat(usdRate.getRateDate()).isEqualTo(LocalDate.now());
    }
}