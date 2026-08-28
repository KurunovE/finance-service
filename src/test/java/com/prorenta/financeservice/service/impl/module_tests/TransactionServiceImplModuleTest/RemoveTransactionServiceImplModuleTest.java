package com.prorenta.financeservice.service.impl.module_tests.TransactionServiceImplModuleTest;

import com.prorenta.financeservice.controller.impl.TransactionControllerImpl;
import com.prorenta.financeservice.exception.GlobalExceptionHandler;
import com.prorenta.financeservice.integration.UserFeignClient;
import com.prorenta.financeservice.mapper.TransactionMapperImpl;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@WebMvcTest
@ContextConfiguration(
        classes = {
                TransactionControllerImpl.class,
                TransactionServiceImpl.class,
                TransactionMapperImpl.class,
                GlobalExceptionHandler.class
        }
)
public class RemoveTransactionServiceImplModuleTest {

    @Autowired
    private MockMvc mockMvc;

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
    @DisplayName("Мягкое удаление транзакции: успешно (HTTP 204)")
    public void softRemoveTransactionSuccessfully() {
        UUID transactionId = UUID.randomUUID();

        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/transactions/{id}", transactionId))
                .andReturn();

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.NO_CONTENT.value());

        Mockito.verify(transactionRepository, Mockito.times(1)).softRemoveTransaction(transactionId);
    }

    @Test
    @SneakyThrows
    @DisplayName("Удаление транзакции: ошибка 400 (Невалидный формат UUID)")
    public void softRemoveTransactionInvalidUuidFormat() {
        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/transactions/{id}", "invalid-uuid"))
                .andReturn();

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        Mockito.verifyNoInteractions(transactionRepository);
    }
}