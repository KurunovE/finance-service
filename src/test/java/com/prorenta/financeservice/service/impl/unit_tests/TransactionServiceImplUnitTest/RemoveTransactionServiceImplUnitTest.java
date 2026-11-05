package com.prorenta.financeservice.service.impl.unit_tests.TransactionServiceImplUnitTest;

import com.prorenta.financeservice.factory.UserInfoDataFactory;
import com.prorenta.financeservice.security.CurrentUserProvider;
import com.prorenta.financeservice.mapper.TransactionMapperImpl;
import com.prorenta.financeservice.repository.TransactionRepository;
import com.prorenta.financeservice.service.CategoryService;
import com.prorenta.financeservice.service.CurrencyService;
import com.prorenta.financeservice.service.TransactionService;
import com.prorenta.financeservice.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                TransactionServiceImpl.class,
                TransactionMapperImpl.class
        }
)
public class RemoveTransactionServiceImplUnitTest {

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
    @DisplayName("Удаление транзакции: успешно")
    public void softRemoveTransactionSuccessfulTest() {
        // Given
        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        UUID transactionId = UUID.randomUUID();
        Mockito.when(transactionRepository.softRemoveTransaction(transactionId, currentUserProvider.getCurrentUserId())).thenReturn(1);

        // When
        transactionService.softRemoveTransaction(transactionId);

        // Then
        Mockito.verify(transactionRepository, Mockito.times(1))
                .softRemoveTransaction(transactionId, currentUserProvider.getCurrentUserId());
    }
}
