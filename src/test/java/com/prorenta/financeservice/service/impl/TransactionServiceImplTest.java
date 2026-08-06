package com.prorenta.financeservice.service.impl;

import com.prorenta.financeservice.exception.CategoryNotFoundException;
import com.prorenta.financeservice.exception.CurrencyNotFoundException;
import com.prorenta.financeservice.mapper.TransactionMapperImpl;
import com.prorenta.financeservice.model.dto.CreateTransactionRequestDto;
import com.prorenta.financeservice.model.dto.TransactionResponseDto;
import com.prorenta.financeservice.model.entity.Category;
import com.prorenta.financeservice.model.entity.Currency;
import com.prorenta.financeservice.model.entity.Transaction;
import com.prorenta.financeservice.repository.TransactionRepository;
import com.prorenta.financeservice.service.CategoryService;
import com.prorenta.financeservice.service.CurrencyService;
import com.prorenta.financeservice.service.TransactionService;
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

import java.awt.*;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        TransactionServiceImpl.class,
        TransactionMapperImpl.class
})
public class TransactionServiceImplTest {

    @Autowired
    private TransactionService transactionService;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private CurrencyService currencyService;

    @MockitoBean
    private TransactionRepository transactionRepository;

    @Test
    @DisplayName("Создание транзакции: успешно")
    public void createTransactionSuccessfulTest() {
        UUID userId = UUID.randomUUID();
        Category category = createDefaultCategory(userId);
        Currency currency = createDefaultCurrency();
        Transaction transaction = createDefaultTransaction(userId, category,currency);
        CreateTransactionRequestDto requestDto = createRequestDto(userId, category, currency);
        TransactionResponseDto expected = createResponseDto(transaction, category, currency);

        Mockito.when(categoryService.findById(Mockito.any(UUID.class))).thenReturn(category);
        Mockito.when(currencyService.findById(Mockito.any(UUID.class))).thenReturn(currency);
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transaction);

        TransactionResponseDto actual = transactionService.createTransaction(requestDto);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Создание транзакции: категория не найдена")
    public void createTransactionCategoryNotFoundTest() {
        UUID userId = UUID.randomUUID();
        CreateTransactionRequestDto requestDto = createRequestDto(
                userId, createDefaultCategory(userId), createDefaultCurrency()
        );
        String message = "Кактегория с id=" + requestDto.categoryId() + " не найдена";

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
        UUID userId = UUID.randomUUID();
        Category category = createDefaultCategory(userId);
        CreateTransactionRequestDto requestDto = createRequestDto(
                userId, category, createDefaultCurrency()
        );
        String message = "Валюта с id=" + requestDto.currencyId() + " не найдена";

        Mockito.when(categoryService.findById(Mockito.any(UUID.class))).thenReturn(category);
        Mockito.when(currencyService.findById(Mockito.any(UUID.class))).thenThrow(new CurrencyNotFoundException(message));

        CurrencyNotFoundException thrown = Assertions.assertThrows(
                CurrencyNotFoundException.class,
                () -> transactionService.createTransaction(requestDto)
        );

        Assertions.assertEquals(message, thrown.getMessage());
    }

    // TODO: тесты для получения транзакций

    @Test
    @DisplayName("Обновление транзакции: успешно")
    public void updateTransactionSuccessfulTest() {

    }

    @Test
    @DisplayName("Обновление транзакции: транзакция не найдена")
    public void updateTransactionTransactionNotFoundTest() {

    }

    @Test
    @DisplayName("Обновление транзакции: категория не найдена")
    public void updateTransactionCategoryNotFoundTest() {

    }

    @Test
    @DisplayName("Обновление транзакции: валюта не найдена")
    public void updateTransactionCurrencyNotFoundTest() {

    }

    @Test
    @DisplayName("Удаление транзакции: успешно")
    public void softRemoveTransactionSuccessfulTest() {


    }

}
