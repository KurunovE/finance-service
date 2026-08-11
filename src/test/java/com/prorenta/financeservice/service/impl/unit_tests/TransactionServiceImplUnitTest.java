package com.prorenta.financeservice.service.impl.unit_tests;

import com.prorenta.financeservice.exception.CategoryNotFoundException;
import com.prorenta.financeservice.exception.CurrencyNotFoundException;
import com.prorenta.financeservice.exception.TransactionNotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.prorenta.financeservice.factory.TransactionDataFactory.*;
import static com.prorenta.financeservice.factory.CurrencyDataFactory.*;
import static com.prorenta.financeservice.factory.CategoryDataFactory.*;
import static com.prorenta.financeservice.factory.UserInfoDataFactory.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        TransactionServiceImpl.class,
        TransactionMapperImpl.class
})
public class TransactionServiceImplUnitTest {

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

    @Test
    @DisplayName("Получение транзакции: успешно")
    public void getTransactionsSuccessfulTest() {
        UserInfoDto userInfoDto = createDefaultUserInfoDto();
        Category category = createDefaultCategory(userInfoDto.id());
        Currency currency = createDefaultCurrency();
        Transaction transaction = createDefaultTransaction(userInfoDto.id(), category, currency);

        FilterTransactionRequestDto filterDto = createFilterTransactionRequestDto(
                "Электроника",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
        );

        Page<Transaction> pageResponse = new PageImpl<>(
                List.of(transaction),
                PageRequest.of(0, 10),
                1);

        Mockito.when(transactionRepository.findAll(Mockito.<Specification<Transaction>>any(), Mockito.any(Pageable.class)))
                .thenReturn(pageResponse);

        FilterTransactionsResponseDto actual = transactionService.getTransactions(filterDto);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(1, actual.countTransactions());
        Assertions.assertEquals(1, actual.countPage());
        Assertions.assertEquals(1, actual.transactions().size());
        Assertions.assertEquals(transaction.getId(), actual.transactions().getFirst().id());
    }

    @Test
    @DisplayName("Получение транзакций: пустой список")
    public void getTransactionsEmptyListTest() {
        FilterTransactionRequestDto filterDto = createFilterTransactionRequestDto(
                "Еда",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
        );

        Page<Transaction> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0);

        Mockito.when(transactionRepository.findAll(Mockito.<Specification<Transaction>>any(), Mockito.any(Pageable.class)))
                .thenReturn(emptyPage);

        FilterTransactionsResponseDto actual = transactionService.getTransactions(filterDto);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(0, actual.countTransactions());
        Assertions.assertEquals(0, actual.countPage());
        Assertions.assertTrue(actual.transactions().isEmpty());
    }

    @Test
    @DisplayName("Обновление транзакции: успешно")
    public void updateTransactionSuccessfulTest() {
        UUID transactionId = UUID.randomUUID();
        UserInfoDto userInfoDto = createDefaultUserInfoDto();
        Category category = createDefaultCategory(userInfoDto.id());
        Currency currency = createDefaultCurrency();

        Transaction existingTransaction = createDefaultTransaction(userInfoDto.id(), category, currency);
        existingTransaction.setId(transactionId);

        UpdateTransactionRequestDto requestDto = UpdateTransactionRequestDto.builder()
                .amount(BigDecimal.valueOf(150.0))
                .description("Новое описание")
                .bank("Новый банк")
                .build();

        Mockito.when(transactionRepository.findActiveTransactionById(transactionId))
                .thenReturn(Optional.of(existingTransaction));
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class)))
                .thenReturn(existingTransaction);

        TransactionResponseDto actual = transactionService.updateTransaction(transactionId, requestDto);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(BigDecimal.valueOf(150.0), actual.amount());
        Assertions.assertEquals("Новое описание", actual.description());
        Assertions.assertEquals("Новый банк", actual.bank());
    }

    @Test
    @DisplayName("Обновление транзакции: транзакция не найдена")
    public void updateTransactionTransactionNotFoundTest() {
        UUID transactionId = UUID.randomUUID();
        UpdateTransactionRequestDto requestDto = UpdateTransactionRequestDto.builder().build();
        String message = "Транзакция с id=" + transactionId + " не найдена";

        Mockito.when(transactionRepository.findActiveTransactionById(transactionId))
                .thenReturn(java.util.Optional.empty());

        TransactionNotFoundException thrown = Assertions.assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.updateTransaction(transactionId, requestDto)
        );

        Assertions.assertEquals(message, thrown.getMessage());
    }

    @Test
    @DisplayName("Обновление транзакции: категория не найдена")
    public void updateTransactionCategoryNotFoundTest() {
        UserInfoDto userInfoDto = createDefaultUserInfoDto();
        Category category = createDefaultCategory(userInfoDto.id());
        Currency currency = createDefaultCurrency();
        Transaction existingTransaction = createDefaultTransaction(userInfoDto.id(), category, currency);
        UUID wrongCategoryId = UUID.randomUUID();
        UpdateTransactionRequestDto requestDto = UpdateTransactionRequestDto.builder()
                .categoryId(wrongCategoryId)
                .build();
        String message = "Категория с id=" + wrongCategoryId + " не найдена";

        Mockito.when(transactionRepository.findActiveTransactionById(existingTransaction.getId()))
                .thenReturn(Optional.of(existingTransaction));
        Mockito.when(categoryService.findById(wrongCategoryId))
                .thenThrow(new CategoryNotFoundException(message));

        CategoryNotFoundException thrown = Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> transactionService.updateTransaction(existingTransaction.getId(), requestDto)
        );

        Assertions.assertEquals(message, thrown.getMessage());
    }

    @Test
    @DisplayName("Обновление транзакции: валюта не найдена")
    public void updateTransactionCurrencyNotFoundTest() {
        UserInfoDto userInfoDto = createDefaultUserInfoDto();
        Category category = createDefaultCategory(userInfoDto.id());
        Currency currency = createDefaultCurrency();

        Transaction existingTransaction = createDefaultTransaction(userInfoDto.id(), category, currency);

        UUID wrongCurrencyId = UUID.randomUUID();
        UpdateTransactionRequestDto requestDto = UpdateTransactionRequestDto.builder()
                .currencyId(wrongCurrencyId)
                .build();
        String message = "Валюта с id=" + wrongCurrencyId + " не найдена";

        Mockito.when(transactionRepository.findActiveTransactionById(existingTransaction.getId()))
                .thenReturn(Optional.of(existingTransaction));
        Mockito.when(currencyService.findById(wrongCurrencyId))
                .thenThrow(new CurrencyNotFoundException(message));

        CurrencyNotFoundException thrown = Assertions.assertThrows(
                CurrencyNotFoundException.class,
                () -> transactionService.updateTransaction(existingTransaction.getId(), requestDto)
        );

        Assertions.assertEquals(message, thrown.getMessage());
    }

    @Test
    @DisplayName("Обновление транзакции: категория принадлежит другому пользователю")
    public void updateTransactionCategoryBelongsToAnotherUserTest() {
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

        Mockito.when(transactionRepository.findActiveTransactionById(existingTransaction.getId()))
                .thenReturn(Optional.of(existingTransaction));
        Mockito.when(categoryService.findById(requestDto.categoryId()))
                .thenReturn(categoryUserB);

        CategoryNotFoundException thrown = Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> transactionService.updateTransaction(existingTransaction.getId(), requestDto)
        );

        Assertions.assertEquals(expectedMessage, thrown.getMessage());
        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any(Transaction.class));
    }

    @Test
    @DisplayName("Обновление транзакции: пустой DTO")
    public void updateTransactionEmptyDtoTest() {
        UserInfoDto userInfoDto = createDefaultUserInfoDto();
        Category category = createDefaultCategory(userInfoDto.id());
        Currency currency = createDefaultCurrency();
        Transaction existingTransaction = createDefaultTransaction(userInfoDto.id(), category, currency);
        UpdateTransactionRequestDto emptyRequestDto = UpdateTransactionRequestDto.builder().build();

        Mockito.when(transactionRepository.findActiveTransactionById(existingTransaction.getId()))
                .thenReturn(Optional.of(existingTransaction));
        Mockito.when(transactionRepository.save(Mockito.any(Transaction.class)))
                .thenReturn(existingTransaction);

        TransactionResponseDto actual = transactionService.updateTransaction(
                existingTransaction.getId(),
                emptyRequestDto
        );

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(existingTransaction.getAmount(), actual.amount());
    }

    @Test
    @DisplayName("Удаление транзакции: успешно")
    public void softRemoveTransactionSuccessfulTest() {
        UUID transactionId = UUID.randomUUID();

        transactionService.softRemoveTransaction(transactionId);

        Mockito.verify(transactionRepository, Mockito.times(1))
                .softRemoveTransaction(transactionId);
    }
}
