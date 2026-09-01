package com.prorenta.financeservice.service.impl.integration_tests.TransactionServiceImplIntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prorenta.financeservice.model.dto.TransactionResponseDto;
import com.prorenta.financeservice.model.dto.UpdateTransactionRequestDto;
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

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

public class UpdateTransactionServiceImplIntegrationTest extends AbstractIntegrationTest {

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
    @DisplayName("Обновлние транзакции: успешно")
    public void updateTransactionSimpleFieldsTest() {
        UUID transactionId = UUID.fromString("44444444-4444-4444-4444-444444444441");

        UpdateTransactionRequestDto requestDto = UpdateTransactionRequestDto.builder()
                .amount(BigDecimal.valueOf(125000.00))
                .description("Повышенная зарплата")
                .bank("Alfa-Bank")
                .build();

        MvcResult mvcResult = mockMvc.perform(patch("/api/v1/transactions/{id}", transactionId)
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
                .isEqualTo(HttpStatus.OK.value());

        Assertions.assertThat(actual.amount()).isEqualByComparingTo("125000.00");
        Assertions.assertThat(actual.description()).isEqualTo("Повышенная зарплата");
        Assertions.assertThat(actual.bank()).isEqualTo("Alfa-Bank");
        Assertions.assertThat(actual.categoryName()).isEqualTo("Зарплата");
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
    @DisplayName("Обновлние транзакции: успешное обновление связанных сущностей")
    public void updateTransactionForeignKeysTest() {
        UUID transactionId = UUID.fromString("44444444-4444-4444-4444-444444444441");
        UUID newCategoryId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        UpdateTransactionRequestDto requestDto = UpdateTransactionRequestDto.builder()
                .categoryId(newCategoryId)
                .build();

        MvcResult mvcResult = mockMvc.perform(patch("/api/v1/transactions/{id}", transactionId)
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
                .isEqualTo(HttpStatus.OK.value());

        Assertions.assertThat(actual.categoryName()).isEqualTo("Продукты");
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
    @DisplayName("Обновлние транзакции: попытка обновить несуществующую или удаленную транзакцию")
    public void updateTransactionNotFoundTest() {
        UUID deletedTransactionId = UUID.fromString("44444444-4444-4444-4444-444444444444");

        UpdateTransactionRequestDto requestDto = UpdateTransactionRequestDto.builder()
                .amount(BigDecimal.valueOf(999.00))
                .build();

        MvcResult mvcResult = mockMvc.perform(patch("/api/v1/transactions/{id}", deletedTransactionId)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND.value());
    }
}
