package com.prorenta.financeservice.service.impl.unit_tests.TransactionServiceImplUnitTest;

import com.prorenta.financeservice.integration.UserFeignClient;
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
@ContextConfiguration(classes = {
        TransactionServiceImpl.class,
        TransactionMapperImpl.class
})
public class TransactionServiceImplRemoveUnitTest {

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
    @DisplayName("Удаление транзакции: успешно")
    public void softRemoveTransactionSuccessfulTest() {
        UUID transactionId = UUID.randomUUID();

        transactionService.softRemoveTransaction(transactionId);

        Mockito.verify(transactionRepository, Mockito.times(1))
                .softRemoveTransaction(transactionId);
    }
}
