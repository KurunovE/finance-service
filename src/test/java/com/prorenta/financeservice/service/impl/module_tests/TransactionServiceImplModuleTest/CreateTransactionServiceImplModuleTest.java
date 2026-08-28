package com.prorenta.financeservice.service.impl.module_tests.TransactionServiceImplModuleTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prorenta.financeservice.controller.impl.TransactionControllerImpl;
import com.prorenta.financeservice.exception.CategoryNotFoundException;
import com.prorenta.financeservice.exception.GlobalExceptionHandler;
import com.prorenta.financeservice.factory.CategoryDataFactory;
import com.prorenta.financeservice.factory.CurrencyDataFactory;
import com.prorenta.financeservice.factory.TransactionDataFactory;
import com.prorenta.financeservice.factory.UserInfoDataFactory;
import com.prorenta.financeservice.integration.UserFeignClient;
import com.prorenta.financeservice.mapper.TransactionMapperImpl;
import com.prorenta.financeservice.model.dto.CreateTransactionRequestDto;
import com.prorenta.financeservice.model.dto.ErrorDto;
import com.prorenta.financeservice.model.dto.TransactionResponseDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest
@ContextConfiguration(
        classes = {
                TransactionControllerImpl.class,
                TransactionServiceImpl.class,
                TransactionMapperImpl.class,
                GlobalExceptionHandler.class
        }
)
public class CreateTransactionServiceImplModuleTest {

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
    @DisplayName("Создание транзакции: успешно (HTTP 201)")
    public void createTransactionSuccessfully() {
        UserInfoDto userInfoDto = UserInfoDataFactory.createDefaultUserInfoDto();
        Category category = CategoryDataFactory.createDefaultCategory(userInfoDto.id());
        Currency currency = CurrencyDataFactory.createDefaultCurrency();

        Transaction savedTransaction = TransactionDataFactory.createDefaultTransaction(userInfoDto.id(), category, currency);
        CreateTransactionRequestDto requestDto = TransactionDataFactory.createRequestDto(userInfoDto.id(), category, currency);
        TransactionResponseDto expected = TransactionDataFactory.createResponseDto(savedTransaction, category, currency);

        Mockito.when(userFeignClient.getUserInfo(Mockito.any(UUID.class)))
                .thenReturn(ResponseEntity.ok(userInfoDto));
        Mockito.when(categoryService.findById(Mockito.any(UUID.class)))
                .thenReturn(category);
        Mockito.when(currencyService.findById(Mockito.any(UUID.class)))
                .thenReturn(currency);
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class)))
                .thenReturn(savedTransaction);

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/transactions")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        TransactionResponseDto actual = objectMapper.readValue(responseContent, TransactionResponseDto.class);

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.CREATED.value());

        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .withEqualsForType(LocalDate::isEqual, LocalDate.class)
                .isEqualTo(expected);
    }

    @Test
    @SneakyThrows
    @DisplayName("Создание транзакции: ошибка валидации DTO (HTTP 400)")
    public void createTransactionWithValidationException() {
        CreateTransactionRequestDto requestDto = CreateTransactionRequestDto.builder()
                .userId(UUID.randomUUID())
                .categoryId(UUID.randomUUID())
                .currencyId(UUID.randomUUID())
                .amount(java.math.BigDecimal.ZERO)
                .createdDate(LocalDate.now())
                .build();

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/transactions")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        Mockito.verifyNoInteractions(transactionRepository);
    }

    @Test
    @SneakyThrows
    @DisplayName("Создание транзакции: бизнес-ошибка - Категория не найдена (HTTP 404)")
    public void createTransactionCategoryNotFound() {
        UserInfoDto userInfoDto = UserInfoDataFactory.createDefaultUserInfoDto();
        Category category = CategoryDataFactory.createDefaultCategory(userInfoDto.id());
        Currency currency = CurrencyDataFactory.createDefaultCurrency();

        CreateTransactionRequestDto requestDto = TransactionDataFactory.createRequestDto(userInfoDto.id(), category, currency);
        String message = "Категория с id=" + requestDto.categoryId() + " не найдена";

        ErrorDto expected = ErrorDto.builder()
                .status(HttpStatus.NOT_FOUND)
                .message(message)
                .build();

        Mockito.when(userFeignClient.getUserInfo(Mockito.any(UUID.class)))
                .thenReturn(ResponseEntity.ok(userInfoDto));
        Mockito.when(categoryService.findById(Mockito.any(UUID.class)))
                .thenThrow(new CategoryNotFoundException(message));

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/transactions")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        ErrorDto actual = objectMapper.readValue(responseContent, ErrorDto.class);

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND.value());

        Assertions.assertThat(actual.message()).isEqualTo(expected.message());
        Mockito.verifyNoInteractions(transactionRepository);
    }
}
