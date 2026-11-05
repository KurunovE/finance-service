package com.prorenta.financeservice.service.impl.integration_tests.TransactionServiceImplIntegrationTest;

import com.prorenta.financeservice.factory.UserInfoDataFactory;
import com.prorenta.financeservice.model.dto.TransactionResponseDto;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.prorenta.financeservice.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prorenta.financeservice.model.dto.FilterTransactionsResponseDto;
import com.prorenta.financeservice.service.impl.integration_tests.AbstractIntegrationTest;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class GetTransactionServiceImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @ParameterizedTest
    @CsvSource({"page,-1", "pageSize,0", "pageSize,101", "pageSize,2147483647"})
    @DisplayName("Некорректная пагинация: ошибка 400")
    void invalidPaginationReturnsBadRequest(String parameter, String value) throws Exception {
        // Given
        Mockito.when(currentUserProvider.getCurrentUserId()).thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        // When
        var result = mockMvc.perform(get("/api/v1/transactions").param(parameter, value)).andReturn();

        // Then
        Assertions.assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @Sql(
            scripts = {
                    "/sql/cleanup.sql",
                    "/sql/insert_transaction_filter_data.sql"
            }
    )
    @DisplayName("Конечная дата включительна, размер страницы 100")
    void endDateAloneIsInclusiveAndMaximumPageSizeIsAccepted() throws Exception {
        // Given
        Mockito.when(currentUserProvider.getCurrentUserId()).thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        // When
        var result = mockMvc.perform(get("/api/v1/transactions")
                .param("endCreatedDate", "2026-08-15")
                .param("pageSize", "100")).andReturn();

        // Then
        Assertions.assertThat(result.getResponse().getStatus()).isEqualTo(200);
        var response = objectMapper.readValue(result.getResponse().getContentAsString(), FilterTransactionsResponseDto.class);
        Assertions.assertThat(response.countTransactions()).isEqualTo(2);
        Assertions.assertThat(response.transactions()).extracting(TransactionResponseDto::createdDate)
                .containsExactly(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 15));
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
    @DisplayName("Получение транзакции: фильтрация без параметров")
    public void getAllActiveTransactionsTest() {
        // Given
        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        // When
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        // Then
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
                    "/sql/insert_transaction_filter_data.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @SneakyThrows
    @DisplayName("Фильтрация транзакций по категории")
    public void getTransactionsByCategoryNameTest() {
        // Given
        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        // When
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/transactions")
                        .param("categoryName", "Продукты")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        // Then
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
                    "/sql/insert_transaction_filter_data.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @SneakyThrows
    @DisplayName("Фильтрация транзакций по датам")
    public void getTransactionsByDateRangeTest() {
        // Given
        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        // When
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/transactions")
                        .param("startCreatedDate", "2026-08-01")
                        .param("endCreatedDate", "2026-08-31")
                        .param("sortDirection", "asc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        // Then
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
