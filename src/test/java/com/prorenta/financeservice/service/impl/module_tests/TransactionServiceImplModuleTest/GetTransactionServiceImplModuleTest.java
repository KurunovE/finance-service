package com.prorenta.financeservice.service.impl.module_tests.TransactionServiceImplModuleTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prorenta.financeservice.controller.impl.TransactionControllerImpl;
import com.prorenta.financeservice.exception.GlobalExceptionHandler;
import com.prorenta.financeservice.factory.CategoryDataFactory;
import com.prorenta.financeservice.factory.CurrencyDataFactory;
import com.prorenta.financeservice.factory.TransactionDataFactory;
import com.prorenta.financeservice.factory.UserInfoDataFactory;
import com.prorenta.financeservice.integration.UserFeignClient;
import com.prorenta.financeservice.mapper.TransactionMapperImpl;
import com.prorenta.financeservice.model.dto.FilterTransactionsResponseDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest
@ContextConfiguration(
        classes = {
                TransactionControllerImpl.class,
                TransactionServiceImpl.class,
                TransactionMapperImpl.class,
                GlobalExceptionHandler.class
        }
)
public class GetTransactionServiceImplModuleTest {

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
    @DisplayName("Получение списка транзакций: успешно (HTTP 200)")
    public void getTransactionsSuccessfully() {
        UserInfoDto userInfoDto = UserInfoDataFactory.createDefaultUserInfoDto();
        Category category = CategoryDataFactory.createDefaultCategory(userInfoDto.id());
        Currency currency = CurrencyDataFactory.createDefaultCurrency();
        Transaction transaction = TransactionDataFactory.createDefaultTransaction(userInfoDto.id(), category, currency);

        Page<Transaction> page = new PageImpl<>(List.of(transaction), PageRequest.of(0, 10), 1);

        Mockito.when(transactionRepository.findAll(Mockito.<Specification<Transaction>>any(), Mockito.any(Pageable.class)))
                .thenReturn(page);

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/transactions")
                        .param("page", "0")
                        .param("pageSize", "10")
                        .param("sortDirection", "DESC")
                        .param("fieldSort", "createdDate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        FilterTransactionsResponseDto actual = objectMapper.readValue(responseContent, FilterTransactionsResponseDto.class);

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.OK.value());

        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.transactions()).hasSize(1);
        Assertions.assertThat(actual.countTransactions()).isEqualTo(1L);
        Assertions.assertThat(actual.transactions().getFirst().id()).isEqualTo(transaction.getId());
    }

    @Test
    @SneakyThrows
    @DisplayName("Получение списка транзакций: пустой список (HTTP 200)")
    public void getTransactionsEmptyList() {
        Page<Transaction> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        Mockito.when(transactionRepository.findAll(Mockito.<Specification<Transaction>>any(), Mockito.any(Pageable.class)))
                .thenReturn(emptyPage);

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        FilterTransactionsResponseDto actual = objectMapper.readValue(responseContent, FilterTransactionsResponseDto.class);

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.OK.value());

        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.transactions()).isEmpty();
        Assertions.assertThat(actual.countTransactions()).isEqualTo(0L);
    }

    @Test
    @SneakyThrows
    @DisplayName("Получение списка транзакций: ошибка валидации DTO (HTTP 400)")
    public void getTransactionsWithValidationException() {
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/transactions")
                        .param("sortDirection", "INVALID_DIR")
                        .param("fieldSort", "INVALID_FIELD")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        Mockito.verifyNoInteractions(transactionRepository);
    }
}
