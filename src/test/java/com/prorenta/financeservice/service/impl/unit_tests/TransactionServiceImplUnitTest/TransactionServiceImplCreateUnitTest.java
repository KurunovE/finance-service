package com.prorenta.financeservice.service.impl.unit_tests.TransactionServiceImplUnitTest;

import com.prorenta.financeservice.exception.CategoryNotFoundException;
import com.prorenta.financeservice.exception.CurrencyNotFoundException;
import com.prorenta.financeservice.exception.UserNotFoundException;
import com.prorenta.financeservice.integration.UserFeignClient;
import com.prorenta.financeservice.mapper.TransactionMapperImpl;
import com.prorenta.financeservice.model.dto.*;
import com.prorenta.financeservice.model.entity.Category;
import com.prorenta.financeservice.model.entity.Currency;
import com.prorenta.financeservice.model.entity.Transaction;
import com.prorenta.financeservice.repository.TransactionRepository;
import com.prorenta.financeservice.service.CategoryService;
import com.prorenta.financeservice.service.CurrencyService;
import com.prorenta.financeservice.service.TransactionService;
import com.prorenta.financeservice.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.prorenta.financeservice.factory.TransactionDataFactory.*;
import static com.prorenta.financeservice.factory.CurrencyDataFactory.*;
import static com.prorenta.financeservice.factory.CategoryDataFactory.*;
import static com.prorenta.financeservice.factory.UserInfoDataFactory.*;

import java.util.UUID;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        TransactionServiceImpl.class,
        TransactionMapperImpl.class
})
public class TransactionServiceImplCreateUnitTest {

    @Autowired
    private TransactionService transactionService;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private CurrencyService currencyService;

    @MockitoBean
    private TransactionRepository transactionRepository;

    @MockitoBean
    private UserFeignClient userFeignClient;

    @Test
    @DisplayName("Создание транзакции: успешно")
    public void createTransactionSuccessfulTest() {
        UserInfoDto userInfoDto = createDefaultUserInfoDto();
        Category category = createDefaultCategory(userInfoDto.id());
        Currency currency = createDefaultCurrency();
        Transaction transaction = createDefaultTransaction(userInfoDto.id(), category, currency);
        CreateTransactionRequestDto requestDto = createRequestDto(userInfoDto.id(), category, currency);
        TransactionResponseDto expected = createResponseDto(transaction, category, currency);

        Mockito.when(userFeignClient.getUserInfo(Mockito.any(UUID.class)))
                .thenReturn(ResponseEntity.ok(userInfoDto));
        Mockito.when(categoryService.findById(Mockito.any(UUID.class)))
                .thenReturn(category);
        Mockito.when(currencyService.findById(Mockito.any(UUID.class)))
                .thenReturn(currency);
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class)))
                .thenReturn(transaction);

        TransactionResponseDto actual = transactionService.createTransaction(requestDto);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Создание транзакции: категория не найдена")
    public void createTransactionCategoryNotFoundTest() {
        UserInfoDto userInfoDto = createDefaultUserInfoDto();
        CreateTransactionRequestDto requestDto = createRequestDto(
                userInfoDto.id(), createDefaultCategory(userInfoDto.id()), createDefaultCurrency()
        );
        String message = "Кактегория с id=" + requestDto.categoryId() + " не найдена";

        Mockito.when(userFeignClient.getUserInfo(Mockito.any(UUID.class)))
                .thenReturn(ResponseEntity.ok(userInfoDto));
        Mockito.when(categoryService.findById(Mockito.any(UUID.class)))
                .thenThrow(new CategoryNotFoundException(message));

        CategoryNotFoundException thrown = Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> transactionService.createTransaction(requestDto)
        );

        Assertions.assertEquals(message, thrown.getMessage());
    }

    @Test
    @DisplayName("Создание транзакции: валюта не найдена")
    public void createTransactionCurrencyNotFoundTest() {
        UserInfoDto userInfoDto = createDefaultUserInfoDto();
        Category category = createDefaultCategory(userInfoDto.id());
        CreateTransactionRequestDto requestDto = createRequestDto(
                userInfoDto.id(), category, createDefaultCurrency()
        );
        String message = "Валюта с id=" + requestDto.currencyId() + " не найдена";

        Mockito.when(userFeignClient.getUserInfo(Mockito.any(UUID.class)))
                .thenReturn(ResponseEntity.ok(userInfoDto));
        Mockito.when(categoryService.findById(Mockito.any(UUID.class)))
                .thenReturn(category);
        Mockito.when(currencyService.findById(Mockito.any(UUID.class)))
                .thenThrow(new CurrencyNotFoundException(message));

        CurrencyNotFoundException thrown = Assertions.assertThrows(
                CurrencyNotFoundException.class,
                () -> transactionService.createTransaction(requestDto)
        );

        Assertions.assertEquals(message, thrown.getMessage());
    }

    @Test
    @DisplayName("Создание транзакции: данные пользователя не найдены")
    public void createTransactionUserNotFoundTest() {
        UUID userId = UUID.randomUUID();
        CreateTransactionRequestDto requestDto = createRequestDto(
                userId, createDefaultCategory(userId), createDefaultCurrency()
        );
        String message = "Данные пользователь с id=" + userId + " не найдены";

        Mockito.when(userFeignClient.getUserInfo(userId))
                .thenReturn(ResponseEntity.ok().body(null));

        UserNotFoundException thrown = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> transactionService.createTransaction(requestDto)
        );

        Assertions.assertEquals(message, thrown.getMessage());
    }
}
