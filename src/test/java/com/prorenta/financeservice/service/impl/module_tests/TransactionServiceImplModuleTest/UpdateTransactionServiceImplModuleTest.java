package com.prorenta.financeservice.service.impl.module_tests.TransactionServiceImplModuleTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prorenta.financeservice.controller.impl.TransactionControllerImpl;
import com.prorenta.financeservice.exception.GlobalExceptionHandler;
import com.prorenta.financeservice.exception.TransactionNotFoundException;
import com.prorenta.financeservice.factory.CategoryDataFactory;
import com.prorenta.financeservice.factory.CurrencyDataFactory;
import com.prorenta.financeservice.factory.TransactionDataFactory;
import com.prorenta.financeservice.factory.UserInfoDataFactory;
import com.prorenta.financeservice.integration.UserFeignClient;
import com.prorenta.financeservice.mapper.TransactionMapperImpl;
import com.prorenta.financeservice.model.dto.ErrorDto;
import com.prorenta.financeservice.model.dto.TransactionResponseDto;
import com.prorenta.financeservice.model.dto.UpdateTransactionRequestDto;
import com.prorenta.financeservice.model.dto.UserInfoDto;
import com.prorenta.financeservice.model.entity.Category;
import com.prorenta.financeservice.model.entity.Currency;
import com.prorenta.financeservice.model.entity.Transaction;
import com.prorenta.financeservice.repository.TransactionRepository;
import com.prorenta.financeservice.service.CategoryService;
import com.prorenta.financeservice.service.CurrencyService;
import com.prorenta.financeservice.service.impl.TransactionServiceImpl;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@WebMvcTest
@ContextConfiguration(
        classes = {
                TransactionControllerImpl.class,
                TransactionServiceImpl.class,
                TransactionMapperImpl.class,
                GlobalExceptionHandler.class
        }
)
public class UpdateTransactionServiceImplModuleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionRepository transactionRepository;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private CurrencyService currencyService;

    @MockitoBean
    private UserFeignClient userFeignClient;

    @Test
    @SneakyThrows
    @DisplayName("Обновление транзакции: успешно (HTTP 200)")
    public void updateTransactionSuccessfully() {
        UUID transactionId = UUID.randomUUID();
        UserInfoDto userInfo = UserInfoDataFactory.createDefaultUserInfoDto();
        Category category = CategoryDataFactory.createDefaultCategory(userInfo.id());
        Currency currency = CurrencyDataFactory.createDefaultCurrency();

        Transaction existingTransaction = TransactionDataFactory.createDefaultTransaction(userInfo.id(), category, currency);
        existingTransaction.setId(transactionId);

        UpdateTransactionRequestDto requestDto = UpdateTransactionRequestDto.builder()
                .amount(BigDecimal.valueOf(450.00))
                .description("Новое описание")
                .build();

        Transaction updatedTransaction = TransactionDataFactory.createDefaultTransaction(userInfo.id(), category, currency);
        updatedTransaction.setId(transactionId);
        updatedTransaction.setAmount(requestDto.amount());
        updatedTransaction.setDescription(requestDto.description());

        Mockito.when(transactionRepository.findActiveTransactionById(transactionId))
                .thenReturn(Optional.of(existingTransaction));
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class)))
                .thenReturn(updatedTransaction);

        MvcResult mvcResult = mockMvc.perform(patch("/api/v1/transactions/{id}", transactionId)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        TransactionResponseDto actual = objectMapper.readValue(responseContent, TransactionResponseDto.class);

        Assertions.assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(actual.amount()).isEqualTo(requestDto.amount());
        Assertions.assertThat(actual.description()).isEqualTo(requestDto.description());
    }

    @Test
    @SneakyThrows
    @DisplayName("Обновление транзакции: транзакция не найдена (HTTP 404)")
    public void updateTransactionNotFound() {
        UUID transactionId = UUID.randomUUID();
        UpdateTransactionRequestDto requestDto = UpdateTransactionRequestDto.builder().build();
        String errorMessage = "Транзакция с id=" + transactionId + " не найдена";

        Mockito.when(transactionRepository.findActiveTransactionById(transactionId))
                .thenThrow(new TransactionNotFoundException(errorMessage));

        MvcResult mvcResult = mockMvc.perform(patch("/api/v1/transactions/{id}", transactionId)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        ErrorDto actualError = objectMapper.readValue(responseContent, ErrorDto.class);

        Assertions.assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        Assertions.assertThat(actualError.message()).isEqualTo(errorMessage);
    }

    @Test
    @SneakyThrows
    @DisplayName("Обновление транзакции: ошибка 400 (Невалидный формат UUID)")
    public void updateTransactionInvalidUuidFormat() {
        MvcResult mvcResult = mockMvc.perform(patch("/api/v1/transactions/{id}", "invalid-uuid")
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        Assertions.assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        Mockito.verifyNoInteractions(transactionRepository);
    }
}
