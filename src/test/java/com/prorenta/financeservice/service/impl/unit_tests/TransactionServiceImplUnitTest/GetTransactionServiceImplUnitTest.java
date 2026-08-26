package com.prorenta.financeservice.service.impl.unit_tests.TransactionServiceImplUnitTest;

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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.prorenta.financeservice.factory.TransactionDataFactory.*;
import static com.prorenta.financeservice.factory.CurrencyDataFactory.*;
import static com.prorenta.financeservice.factory.CategoryDataFactory.*;
import static com.prorenta.financeservice.factory.UserInfoDataFactory.*;

import java.time.LocalDate;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                TransactionServiceImpl.class,
                TransactionMapperImpl.class
        }
)
public class GetTransactionServiceImplUnitTest {

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
}
