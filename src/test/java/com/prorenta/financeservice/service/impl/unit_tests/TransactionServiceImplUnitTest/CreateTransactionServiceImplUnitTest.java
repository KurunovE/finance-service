package com.prorenta.financeservice.service.impl.unit_tests.TransactionServiceImplUnitTest;

import com.prorenta.financeservice.factory.UserInfoDataFactory;
import com.prorenta.financeservice.security.CurrentUserProvider;
import com.prorenta.financeservice.exception.CategoryNotFoundException;
import com.prorenta.financeservice.exception.CurrencyNotFoundException;
import com.prorenta.financeservice.exception.UserNotFoundException;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.prorenta.financeservice.factory.TransactionDataFactory.*;
import static com.prorenta.financeservice.factory.CurrencyDataFactory.*;
import static com.prorenta.financeservice.factory.CategoryDataFactory.*;
import static com.prorenta.financeservice.factory.UserInfoDataFactory.*;

import java.util.UUID;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                TransactionServiceImpl.class,
                TransactionMapperImpl.class
        }
)
public class CreateTransactionServiceImplUnitTest {

    @Autowired
    private TransactionService transactionService;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private CurrencyService currencyService;

    @MockitoBean
    private TransactionRepository transactionRepository;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @Test
    @DisplayName("Создание транзакции: успешно")
    public void createTransactionSuccessfulTest() {
        // Given
        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        UserInfoDto userInfoDto = createDefaultUserInfoDto();
        Category category = createDefaultCategory(userInfoDto.userId());
        Currency currency = createDefaultCurrency();
        Transaction transaction = createDefaultTransaction(userInfoDto.userId(), category, currency);
        CreateTransactionRequestDto requestDto = createRequestDto(category, currency);
        TransactionResponseDto expected = createResponseDto(transaction, category, currency);

        Mockito.when(categoryService.findAvailableCategoryById(Mockito.any(UUID.class)))
                .thenReturn(category);
        Mockito.when(currencyService.findById(Mockito.any(UUID.class)))
                .thenReturn(currency);
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class)))
                .thenReturn(transaction);

        // When
        TransactionResponseDto actual = transactionService.createTransaction(requestDto);

        // Then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Создание транзакции: категория не найдена")
    public void createTransactionCategoryNotFoundTest() {
        // Given
        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        UserInfoDto userInfoDto = createDefaultUserInfoDto();
        CreateTransactionRequestDto requestDto = createRequestDto(
                createDefaultCategory(userInfoDto.userId()), createDefaultCurrency()
        );
        String message = "Категория с id=" + requestDto.categoryId() + " не найдена";

        Mockito.when(categoryService.findAvailableCategoryById(Mockito.any(UUID.class)))
                .thenThrow(new CategoryNotFoundException(message));

        // When
        CategoryNotFoundException thrown = Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> transactionService.createTransaction(requestDto)
        );

        // Then
        Assertions.assertEquals(message, thrown.getMessage());
    }

    @Test
    @DisplayName("Создание транзакции: валюта не найдена")
    public void createTransactionCurrencyNotFoundTest() {
        // Given
        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        UserInfoDto userInfoDto = createDefaultUserInfoDto();
        Category category = createDefaultCategory(userInfoDto.userId());
        CreateTransactionRequestDto requestDto = createRequestDto(
                category, createDefaultCurrency()
        );
        String message = "Валюта с id=" + requestDto.currencyId() + " не найдена";

        Mockito.when(categoryService.findAvailableCategoryById(Mockito.any(UUID.class)))
                .thenReturn(category);
        Mockito.when(currencyService.findById(Mockito.any(UUID.class)))
                .thenThrow(new CurrencyNotFoundException(message));

        // When
        CurrencyNotFoundException thrown = Assertions.assertThrows(
                CurrencyNotFoundException.class,
                () -> transactionService.createTransaction(requestDto)
        );

        // Then
        Assertions.assertEquals(message, thrown.getMessage());
    }

    @Test
    @DisplayName("Создание транзакции: данные пользователя не найдены")
    public void createTransactionUserNotFoundTest() {
        // Given
        UUID userId = UserInfoDataFactory.DEFAULT_USER_ID;
        CreateTransactionRequestDto requestDto = createRequestDto(
                createDefaultCategory(userId), createDefaultCurrency()
        );
        String message = "Данные пользователь с id=" + userId + " не найдены";

        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenThrow(new UserNotFoundException(message));

        // When
        UserNotFoundException thrown = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> transactionService.createTransaction(requestDto)
        );

        // Then
        Assertions.assertEquals(message, thrown.getMessage());
    }
}
