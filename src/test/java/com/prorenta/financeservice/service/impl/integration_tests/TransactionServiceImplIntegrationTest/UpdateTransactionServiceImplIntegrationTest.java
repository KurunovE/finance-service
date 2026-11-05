package com.prorenta.financeservice.service.impl.integration_tests.TransactionServiceImplIntegrationTest;

import com.prorenta.financeservice.factory.UserInfoDataFactory;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.prorenta.financeservice.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prorenta.financeservice.model.dto.TransactionResponseDto;
import com.prorenta.financeservice.model.dto.UpdateTransactionRequestDto;
import com.prorenta.financeservice.service.impl.integration_tests.AbstractIntegrationTest;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.prorenta.financeservice.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

public class UpdateTransactionServiceImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    static Stream<UpdateTransactionRequestDto> invalidUpdates() {
        return Stream.of(
                UpdateTransactionRequestDto.builder().amount(new BigDecimal("-10")).build(),
                UpdateTransactionRequestDto.builder().amount(BigDecimal.ZERO).build(),
                UpdateTransactionRequestDto.builder().amount(new BigDecimal("0.014")).build(),
                UpdateTransactionRequestDto.builder().amount(new BigDecimal("10000000000000")).build(),
                UpdateTransactionRequestDto.builder().createdDate(LocalDate.now().plusDays(1)).build(),
                UpdateTransactionRequestDto.builder().bank("x".repeat(51)).build(),
                UpdateTransactionRequestDto.builder().description("x".repeat(256)).build()
        );
    }

    @ParameterizedTest
    @MethodSource("invalidUpdates")
    @Sql(scripts = {"/sql/cleanup.sql", "/sql/insert_transaction_filter_data.sql"})
    @DisplayName("Некорректное обновление сохраняет транзакцию")
    void invalidPatchDoesNotChangeTransaction(UpdateTransactionRequestDto request) throws Exception {
        // Given
        UUID id = UUID.fromString("44444444-4444-4444-4444-444444444441");
        Mockito.when(currentUserProvider.getCurrentUserId()).thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        // When
        MvcResult result = mockMvc.perform(patch("/api/v1/transactions/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andReturn();

        // Then
        Assertions.assertThat(result.getResponse().getStatus()).isEqualTo(400);
        var transaction = transactionRepository.findById(id).orElseThrow();
        Assertions.assertThat(transaction.getAmount()).isEqualByComparingTo("100000.00");
        Assertions.assertThat(transaction.getCreatedDate()).isEqualTo(LocalDate.of(2026, 8, 1));
        Assertions.assertThat(transaction.getBank()).isEqualTo("T-Bank");
        Assertions.assertThat(transaction.getDescription()).isEqualTo("ЗП");
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
    @DisplayName("Обновление транзакции: успешно")
    public void updateTransactionSimpleFieldsTest() {
        // Given
        UUID transactionId = UUID.fromString("44444444-4444-4444-4444-444444444441");

        UpdateTransactionRequestDto requestDto = UpdateTransactionRequestDto.builder()
                .amount(BigDecimal.valueOf(125000.00))
                .description("Повышенная зарплата")
                .bank("Alfa-Bank")
                .build();

        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        // When
        MvcResult mvcResult = mockMvc.perform(patch("/api/v1/transactions/{id}", transactionId)
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
                    "/sql/insert_transaction_filter_data.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @SneakyThrows
    @DisplayName("Обновление категории транзакции")
    public void updateTransactionForeignKeysTest() {
        // Given
        UUID transactionId = UUID.fromString("44444444-4444-4444-4444-444444444441");
        UUID newCategoryId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        UpdateTransactionRequestDto requestDto = UpdateTransactionRequestDto.builder()
                .categoryId(newCategoryId)
                .build();

        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        // When
        MvcResult mvcResult = mockMvc.perform(patch("/api/v1/transactions/{id}", transactionId)
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
                .isEqualTo(HttpStatus.OK.value());

        Assertions.assertThat(actual.categoryName()).isEqualTo("Продукты");
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
    @DisplayName("Обновление транзакции: ошибка 404")
    public void updateTransactionNotFoundTest() {
        // Given
        UUID deletedTransactionId = UUID.fromString("44444444-4444-4444-4444-444444444444");

        UpdateTransactionRequestDto requestDto = UpdateTransactionRequestDto.builder()
                .amount(BigDecimal.valueOf(999.00))
                .build();

        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        // When
        MvcResult mvcResult = mockMvc.perform(patch("/api/v1/transactions/{id}", deletedTransactionId)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        // Then
        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND.value());
    }
}
