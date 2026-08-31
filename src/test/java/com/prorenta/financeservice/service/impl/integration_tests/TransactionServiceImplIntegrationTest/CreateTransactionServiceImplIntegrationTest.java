package com.prorenta.financeservice.service.impl.integration_tests.TransactionServiceImplIntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prorenta.financeservice.factory.CategoryDataFactory;
import com.prorenta.financeservice.factory.CurrencyDataFactory;
import com.prorenta.financeservice.factory.UserInfoDataFactory;
import com.prorenta.financeservice.service.impl.integration_tests.AbstractIntegrationTest;
import com.prorenta.financeservice.service.impl.integration_tests.util.UserClientHelper;
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
    private UserClientHelper userClientHelper;

    @Autowired
    private ObjectMapper objectMapper;

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
        UUID userId = UserInfoDataFactory.DEFAULT_USER_ID;
        UUID categoryId = CategoryDataFactory.DEFAULT_CATEGORY_ID;
        UUID currencyId = CurrencyDataFactory.DEFAULT_CURRENCY_ID;

        CreateTransactionRequestDto requestDto = CreateTransactionRequestDto.builder()
                .userId(userId)
                .categoryId(categoryId)
                .currencyId(currencyId)
                .amount(BigDecimal.valueOf(1500.50))
                .bank("T-Bank")
                .description("Покупка продуктов")
                .createdDate(LocalDate.now())
                .build();

        userClientHelper.mockUserInfo(userId);

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/transactions")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

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
}
