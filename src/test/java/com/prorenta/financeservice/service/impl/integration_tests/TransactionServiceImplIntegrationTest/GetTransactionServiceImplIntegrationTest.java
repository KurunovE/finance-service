package com.prorenta.financeservice.service.impl.integration_tests.TransactionServiceImplIntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prorenta.financeservice.model.dto.FilterTransactionsResponseDto;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class GetTransactionServiceImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Sql(
            scripts = {
                    "/sql/cleanup.sql",
                    "/sql/insert_filter_data.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @SneakyThrows
    @DisplayName("Получение транзакций: фильтрация без параметров")
    public void getAllActiveTransactionsTest() {
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        FilterTransactionsResponseDto actual = objectMapper.readValue(
                responseContent,
                FilterTransactionsResponseDto.class
        );

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.OK.value());

        Assertions.assertThat(actual.countTransactions()).isEqualTo(3);
        Assertions.assertThat(actual.transactions()).hasSize(3);
    }

    @Test
    @Sql(
            scripts = {
                    "/sql/cleanup.sql",
                    "/sql/insert_filter_data.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @SneakyThrows
    @DisplayName("Получение транзакций: фильтрация строго по имени категории")
    public void getTransactionsByCategoryNameTest() {
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/transactions")
                        .param("categoryName", "Продукты")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        FilterTransactionsResponseDto actual = objectMapper.readValue(
                responseContent,
                FilterTransactionsResponseDto.class
        );

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.OK.value());

        Assertions.assertThat(actual.countTransactions()).isEqualTo(2);
        Assertions.assertThat(actual.transactions())
                .allSatisfy(transaction ->
                        Assertions.assertThat(transaction.categoryName()).isEqualTo("Продукты")
                );
    }

    @Test
    @Sql(
            scripts = {
                    "/sql/cleanup.sql",
                    "/sql/insert_filter_data.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @SneakyThrows
    @DisplayName("Получение транзакций: фильтрация по диапазону дат с пагинацией и сортировкой")
    public void getTransactionsByDateRangeTest() {
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/transactions")
                        .param("startCreatedDate", "2026-08-01")
                        .param("endCreatedDate", "2026-08-31")
                        .param("sortDirection", "asc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        FilterTransactionsResponseDto actual = objectMapper.readValue(
                responseContent,
                FilterTransactionsResponseDto.class
        );

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.OK.value());

        Assertions.assertThat(actual.countTransactions()).isEqualTo(2);
        Assertions.assertThat(actual.transactions().get(0).categoryName()).isEqualTo("Зарплата");
        Assertions.assertThat(actual.transactions().get(1).categoryName()).isEqualTo("Продукты");
    }

}
