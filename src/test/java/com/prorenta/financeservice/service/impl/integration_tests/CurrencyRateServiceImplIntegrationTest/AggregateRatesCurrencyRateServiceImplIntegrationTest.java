package com.prorenta.financeservice.service.impl.integration_tests.CurrencyRateServiceImplIntegrationTest;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.prorenta.financeservice.security.CurrentUserProvider;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.prorenta.financeservice.factory.CbrApiDataFactory;
import com.prorenta.financeservice.model.entity.CurrencyRate;
import com.prorenta.financeservice.repository.CurrencyRateRepository;
import com.prorenta.financeservice.service.CurrencyRateService;
import com.prorenta.financeservice.service.impl.integration_tests.AbstractIntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import com.prorenta.financeservice.scheduler.CurrencyRatesScheduler;
import com.prorenta.financeservice.model.dto.ListCurrencyRatesResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Autowired
    private CurrencyRatesScheduler scheduler;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 100})
    @Sql(scripts = {"/sql/cleanup.sql", "/sql/insert_currency.sql"})
    void persistsNominalAndResponseDateAndUpdatesSameQuote(int nominal) throws Exception {
        String xml = CbrApiDataFactory.cbrFeignClientResponse.replace("<Nominal>1</Nominal>",
                "<Nominal>" + nominal + "</Nominal>");
        WireMock.stubFor(get(urlPathEqualTo("/XML_daily.asp"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/xml; charset=windows-1251")
                        .withBody(xml)));

        currencyRateService.aggregateRates();
        var original = currencyRateRepository.findAll().getFirst();
        WireMock.stubFor(get(urlPathEqualTo("/XML_daily.asp"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/xml; charset=windows-1251")
                        .withBody(xml.replace("89,5012", "91,2500"))));
        currencyRateService.aggregateRates();

        var saved = currencyRateRepository.findAll();
        Assertions.assertThat(saved).hasSize(1);
        Assertions.assertThat(saved.getFirst().getId()).isEqualTo(original.getId());
        Assertions.assertThat(saved.getFirst().getNominal()).isEqualTo(nominal);
        Assertions.assertThat(saved.getFirst().getRateDate()).isEqualTo(LocalDate.of(2026, 9, 1));
        Assertions.assertThat(saved.getFirst().getRate()).isEqualByComparingTo("91.2500");

        var result = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/api/v1/currencies/USD/rates")
                .param("startDate", "2026-09-01").param("endDate", "2026-09-01")).andReturn();
        Assertions.assertThat(result.getResponse().getStatus()).isEqualTo(200);
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), ListCurrencyRatesResponseDto.class);
        Assertions.assertThat(response.rates()).hasSize(1);
        Assertions.assertThat(response.rates().getFirst().nominal()).isEqualTo(nominal);
        Assertions.assertThat(response.rates().getFirst().date()).isEqualTo(LocalDate.of(2026, 9, 1));
        Assertions.assertThat(response.rates().getFirst().rate()).isEqualByComparingTo("91.2500");
    }

    @Test
    @Sql(scripts = {"/sql/cleanup.sql", "/sql/insert_currency.sql"})
    void schedulerUsesDatabaseLockAndSkipsSecondInvocation() {
        jdbcTemplate.update("DELETE FROM shedlock WHERE name = ?", "currency-rates");
        WireMock.resetAllRequests();
        WireMock.stubFor(get(urlPathEqualTo("/XML_daily.asp"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/xml; charset=windows-1251")
                        .withBody(CbrApiDataFactory.cbrFeignClientResponse)));

        scheduler.scheduleCurrencySync();
        scheduler.scheduleCurrencySync();

        WireMock.verify(1, WireMock.getRequestedFor(urlPathEqualTo("/XML_daily.asp")));
        Assertions.assertThat(currencyRateRepository.findAll()).hasSize(1);
        Assertions.assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM shedlock WHERE name = ? AND lock_until > (CURRENT_TIMESTAMP AT TIME ZONE 'UTC')",
                Integer.class, "currency-rates")).isEqualTo(1);
    }

    @Test
    @Sql(
            scripts = {
                    "/sql/cleanup.sql",
                    "/sql/insert_currency.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @DisplayName("Интеграционный тест: успешная агрегация курсов валют")
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
        Assertions.assertThat(usdRate.getNominal()).isEqualTo(1);
        Assertions.assertThat(usdRate.getRateDate()).isEqualTo(LocalDate.of(2026, 9, 1));
    }
}
