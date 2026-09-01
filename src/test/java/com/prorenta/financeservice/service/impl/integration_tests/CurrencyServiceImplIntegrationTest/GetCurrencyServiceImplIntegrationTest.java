package com.prorenta.financeservice.service.impl.integration_tests.CurrencyServiceImplIntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prorenta.financeservice.model.dto.CurrencyResponseDto;
import com.prorenta.financeservice.model.dto.ErrorDto;
import com.prorenta.financeservice.model.dto.ListCurrenciesResponseDto;
import com.prorenta.financeservice.service.impl.integration_tests.AbstractIntegrationTest;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class GetCurrencyServiceImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Sql(
            scripts = {
                    "/sql/cleanup.sql",
                    "/sql/insert_transaction_filter_data.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @SneakyThrows
    @DisplayName("Получение валюты: успешное получение активных валют")
    public void getActiveCurrenciesTest() {
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        ListCurrenciesResponseDto actual = objectMapper.readValue(
                responseContent,
                ListCurrenciesResponseDto.class
        );

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.OK.value());

        Assertions.assertThat(actual.currencies()).hasSize(1);
        Assertions.assertThat(actual.currencies().getFirst().code()).isEqualTo("RUB");
    }

    @Test
    @Sql(
            scripts = {
                    "/sql/cleanup.sql",
                    "/sql/insert_transaction_filter_data.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @SneakyThrows
    @DisplayName("Получение валюты: успешное получение валюты по ID")
    public void getCurrencyByIdTest() {
        UUID currencyId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/currencies/{id}", currencyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        CurrencyResponseDto actual = objectMapper.readValue(
                responseContent,
                CurrencyResponseDto.class
        );

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(actual.id()).isEqualTo(currencyId);
        Assertions.assertThat(actual.code()).isEqualTo("RUB");
    }

    @Test
    @SneakyThrows
    @DisplayName("Получение валюты: получение несуществующей валюты")
    public void getCurrencyByIdNotFoundTest() {
        UUID unknownCurrencyId = UUID.randomUUID();

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/currencies/{id}", unknownCurrencyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        ErrorDto actualError = objectMapper.readValue(
                responseContent,
                ErrorDto.class
        );

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND.value());

        Assertions.assertThat(actualError.message())
                .isEqualTo("Валюта с id=" + unknownCurrencyId + " не найдена");
    }
}
