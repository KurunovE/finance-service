package com.prorenta.financeservice.service.impl.unit_tests.TransactionServiceImplUnitTest;

import com.prorenta.financeservice.factory.UserInfoDataFactory;
import com.prorenta.financeservice.security.CurrentUserProvider;
import com.prorenta.financeservice.exception.CategoryNotFoundException;
import com.prorenta.financeservice.exception.CurrencyNotFoundException;
import com.prorenta.financeservice.exception.TransactionNotFoundException;
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

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                TransactionServiceImpl.class,
                TransactionMapperImpl.class
        }
)
public class UpdateTransactionServiceImplUnitTest {

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
    @DisplayName("Обновление транзакции: успешно")
    public void updateTransactionSuccessfulTest() {
        // Given
        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        UUID transactionId = UUID.randomUUID();
        UserInfoDto userInfoDto = createDefaultUserInfoDto();
        Category category = createDefaultCategory(userInfoDto.userId());
        Currency currency = createDefaultCurrency();

        Transaction existingTransaction = createDefaultTransaction(userInfoDto.userId(), category, currency);
        existingTransaction.setId(transactionId);

        UpdateTransactionRequestDto requestDto = UpdateTransactionRequestDto.builder()
                .amount(BigDecimal.valueOf(150.0))
                .description("Новое описание")
                .bank("Новый банк")
                .build();

        Mockito.when(transactionRepository.findActiveTransactionByIdAndUserId(transactionId, currentUserProvider.getCurrentUserId()))
                .thenReturn(Optional.of(existingTransaction));
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class)))
                .thenReturn(existingTransaction);

        // When
        TransactionResponseDto actual = transactionService.updateTransaction(transactionId, requestDto);

        // Then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(BigDecimal.valueOf(150.0), actual.amount());
        Assertions.assertEquals("Новое описание", actual.description());
        Assertions.assertEquals("Новый банк", actual.bank());
    }

    @Test
    @DisplayName("Обновление транзакции: транзакция не найдена")
    public void updateTransactionTransactionNotFoundTest() {
        // Given
        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        UUID transactionId = UUID.randomUUID();
        UpdateTransactionRequestDto requestDto = UpdateTransactionRequestDto.builder().build();
        String message = "Транзакция с id=" + transactionId + " не найдена";

        Mockito.when(transactionRepository.findActiveTransactionByIdAndUserId(transactionId, currentUserProvider.getCurrentUserId()))
                .thenReturn(java.util.Optional.empty());

        // When
        TransactionNotFoundException thrown = Assertions.assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.updateTransaction(transactionId, requestDto)
        );

        // Then
        Assertions.assertEquals(message, thrown.getMessage());
    }

    @Test
    @DisplayName("Обновление транзакции: категория не найдена")
    public void updateTransactionCategoryNotFoundTest() {
        // Given
        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        UserInfoDto userInfoDto = createDefaultUserInfoDto();
        Category category = createDefaultCategory(userInfoDto.userId());
        Currency currency = createDefaultCurrency();
        Transaction existingTransaction = createDefaultTransaction(userInfoDto.userId(), category, currency);
        UUID wrongCategoryId = UUID.randomUUID();
        UpdateTransactionRequestDto requestDto = UpdateTransactionRequestDto.builder()
                .categoryId(wrongCategoryId)
                .build();
        String message = "Категория с id=" + wrongCategoryId + " не найдена";

        Mockito.when(transactionRepository.findActiveTransactionByIdAndUserId(existingTransaction.getId(), currentUserProvider.getCurrentUserId()))
                .thenReturn(Optional.of(existingTransaction));
        Mockito.when(categoryService.findAvailableCategoryById(wrongCategoryId))
                .thenThrow(new CategoryNotFoundException(message));

        // When
        CategoryNotFoundException thrown = Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> transactionService.updateTransaction(existingTransaction.getId(), requestDto)
        );

        // Then
        Assertions.assertEquals(message, thrown.getMessage());
    }

    @Test
    @DisplayName("Обновление транзакции: валюта не найдена")
    public void updateTransactionCurrencyNotFoundTest() {
        // Given
        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        UserInfoDto userInfoDto = createDefaultUserInfoDto();
        Category category = createDefaultCategory(userInfoDto.userId());
        Currency currency = createDefaultCurrency();

        Transaction existingTransaction = createDefaultTransaction(userInfoDto.userId(), category, currency);

        UUID wrongCurrencyId = UUID.randomUUID();
        UpdateTransactionRequestDto requestDto = UpdateTransactionRequestDto.builder()
                .currencyId(wrongCurrencyId)
                .build();
        String message = "Валюта с id=" + wrongCurrencyId + " не найдена";

        Mockito.when(transactionRepository.findActiveTransactionByIdAndUserId(existingTransaction.getId(), currentUserProvider.getCurrentUserId()))
                .thenReturn(Optional.of(existingTransaction));
        Mockito.when(currencyService.findById(wrongCurrencyId))
                .thenThrow(new CurrencyNotFoundException(message));

        // When
        CurrencyNotFoundException thrown = Assertions.assertThrows(
                CurrencyNotFoundException.class,
                () -> transactionService.updateTransaction(existingTransaction.getId(), requestDto)
        );

        // Then
        Assertions.assertEquals(message, thrown.getMessage());
    }

    @Test
    @DisplayName("Обновление транзакции с чужой категорией")
    public void updateTransactionCategoryBelongsToAnotherUserTest() {
        // Given
        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();
        Transaction existingTransaction = createDefaultTransaction(
                userA,
                createDefaultCategory(userA),
                createDefaultCurrency()
        );
        Category categoryUserB = createDefaultCategory(userB);
        UpdateTransactionRequestDto requestDto = UpdateTransactionRequestDto.builder()
                .categoryId(categoryUserB.getId())
                .build();
        String expectedMessage = "Категория с id=" + requestDto.categoryId() + " не найдена";

        Mockito.when(transactionRepository.findActiveTransactionByIdAndUserId(existingTransaction.getId(), currentUserProvider.getCurrentUserId()))
                .thenReturn(Optional.of(existingTransaction));
        Mockito.when(categoryService.findAvailableCategoryById(requestDto.categoryId()))
                .thenReturn(categoryUserB);

        // When
        CategoryNotFoundException thrown = Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> transactionService.updateTransaction(existingTransaction.getId(), requestDto)
        );

        // Then
        Assertions.assertEquals(expectedMessage, thrown.getMessage());
        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any(Transaction.class));
    }

    @Test
    @DisplayName("Обновление транзакции: пустой DTO")
    public void updateTransactionEmptyDtoTest() {
        // Given
        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        UserInfoDto userInfoDto = createDefaultUserInfoDto();
        Category category = createDefaultCategory(userInfoDto.userId());
        Currency currency = createDefaultCurrency();
        Transaction existingTransaction = createDefaultTransaction(userInfoDto.userId(), category, currency);
        UpdateTransactionRequestDto emptyRequestDto = UpdateTransactionRequestDto.builder().build();

        Mockito.when(transactionRepository.findActiveTransactionByIdAndUserId(existingTransaction.getId(), currentUserProvider.getCurrentUserId()))
                .thenReturn(Optional.of(existingTransaction));
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class)))
                .thenReturn(existingTransaction);

        // When
        TransactionResponseDto actual = transactionService.updateTransaction(
                existingTransaction.getId(),
                emptyRequestDto
        );

        // Then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(existingTransaction.getAmount(), actual.amount());
    }
}
