package com.prorenta.financeservice.service.impl.integration_tests.TransactionServiceImplIntegrationTest;

import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.prorenta.financeservice.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prorenta.financeservice.factory.CategoryDataFactory;
import com.prorenta.financeservice.factory.CurrencyDataFactory;
import com.prorenta.financeservice.factory.UserInfoDataFactory;
import com.prorenta.financeservice.service.impl.integration_tests.AbstractIntegrationTest;
import com.prorenta.financeservice.model.dto.CreateTransactionRequestDto;
import com.prorenta.financeservice.model.dto.TransactionResponseDto;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class CreateTransactionServiceImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @Test
    @Sql(
            scripts = {
                    "/sql/cleanup.sql",
                    "/sql/insert_currency.sql",
                    "/sql/insert_category.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @SneakyThrows
    @DisplayName("Создание транзакции: успешно")
    public void createTransactionSuccessfulTest() {
        // Given
        UUID categoryId = CategoryDataFactory.DEFAULT_CATEGORY_ID;
        UUID currencyId = CurrencyDataFactory.DEFAULT_CURRENCY_ID;

        CreateTransactionRequestDto requestDto = CreateTransactionRequestDto.builder()
                .categoryId(categoryId)
                .currencyId(currencyId)
                .amount(BigDecimal.valueOf(1500.50))
                .bank("T-Bank")
                .description("Покупка продуктов")
                .createdDate(LocalDate.now())
                .build();

        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        // When
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/transactions")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        // Then
        String responseContent = mvcResult.getResponse().getContentAsString();

        TransactionResponseDto actual = objectMapper.readValue(
                responseContent,
                TransactionResponseDto.class
        );

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.CREATED.value());

        Assertions.assertThat(actual.amount()).isEqualByComparingTo("1500.50");
        Assertions.assertThat(actual.description()).isEqualTo("Покупка продуктов");
        Assertions.assertThat(actual.bank()).isEqualTo("T-Bank");
        Assertions.assertThat(actual.categoryName()).isNotBlank();
        Assertions.assertThat(actual.currencyCode()).isNotBlank();
    }

    @Test
    @Sql(
            scripts = {
                    "/sql/cleanup.sql",
                    "/sql/insert_currency.sql",
                    "/sql/insert_category.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @SneakyThrows
    @DisplayName("Создание транзакции: отсутствует профиль текущего пользователя")
    public void createTransactionMissingUserProfileTest() {
        // Given
        UUID categoryId = CategoryDataFactory.DEFAULT_CATEGORY_ID;
        UUID currencyId = CurrencyDataFactory.DEFAULT_CURRENCY_ID;

        CreateTransactionRequestDto requestDto = CreateTransactionRequestDto.builder()
                .categoryId(categoryId)
                .currencyId(currencyId)
                .amount(java.math.BigDecimal.valueOf(1500.50))
                .bank("T-Bank")
                .description("Покупка продуктов")
                .createdDate(java.time.LocalDate.now())
                .build();

        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenThrow(new com.prorenta.financeservice.exception.UserNotFoundException("Профиль пользователя не найден"));

        // When
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/transactions")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        // Then
        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND.value());
    }
}
