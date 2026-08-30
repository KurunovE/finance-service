package com.prorenta.financeservice.service.impl.module_tests.CurrencyServiceImplModuleTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prorenta.financeservice.controller.impl.CurrencyControllerImpl;
import com.prorenta.financeservice.exception.GlobalExceptionHandler;
import com.prorenta.financeservice.factory.CurrencyDataFactory;
import com.prorenta.financeservice.mapper.CurrencyMapperImpl;
import com.prorenta.financeservice.model.dto.CurrencyResponseDto;
import com.prorenta.financeservice.model.dto.ErrorDto;
import com.prorenta.financeservice.model.dto.ListCurrenciesResponseDto;
import com.prorenta.financeservice.model.entity.Currency;
import com.prorenta.financeservice.repository.CurrencyRepository;
import com.prorenta.financeservice.service.impl.CurrencyServiceImpl;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest
@ContextConfiguration(
        classes = {
                CurrencyControllerImpl.class,
                CurrencyServiceImpl.class,
                CurrencyMapperImpl.class,
                GlobalExceptionHandler.class
        }
)
public class GetCurrencyServiceImplModuleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CurrencyRepository currencyRepository;

    @Test
    @SneakyThrows
    @DisplayName("Получение списка валют: успешно")
    public void getCurrenciesSuccessfully() {
        Currency currency = CurrencyDataFactory.createDefaultCurrency();

        Mockito.when(currencyRepository.findAllActiveCurrencies())
                .thenReturn(List.of(currency));

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();

        ListCurrenciesResponseDto actual = objectMapper.readValue(
                responseContent,
                ListCurrenciesResponseDto.class
        );

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.OK.value());

        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.currencies()).hasSize(1);
        Assertions.assertThat(actual.currencies().getFirst().id()).isEqualTo(currency.getId());

        Mockito.verify(currencyRepository, Mockito.times(1)).findAllActiveCurrencies();
    }

    @Test
    @SneakyThrows
    @DisplayName("Получение списка валют: пустой список")
    public void getCurrenciesEmptyList() {
        Mockito.when(currencyRepository.findAllActiveCurrencies())
                .thenReturn(List.of());

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();

        ListCurrenciesResponseDto actual = objectMapper.readValue(
                responseContent,
                ListCurrenciesResponseDto.class
        );

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.OK.value());

        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.currencies()).isEmpty();
    }

    @Test
    @SneakyThrows
    @DisplayName("Получение валюты по ID: успешно")
    public void getCurrencyByIdSuccessfully() {
        Currency currency = CurrencyDataFactory.createDefaultCurrency();
        UUID currencyId = currency.getId();

        Mockito.when(currencyRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(currency));

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/currencies/{currencyId}", currencyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();

        CurrencyResponseDto actual = objectMapper.readValue(
                responseContent,
                CurrencyResponseDto.class
        );

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.OK.value());

        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.id()).isEqualTo(currency.getId());
        Assertions.assertThat(actual.code()).isEqualTo(currency.getCode());

        Mockito.verify(currencyRepository, Mockito.times(1)).findById(currencyId);
    }

    @Test
    @SneakyThrows
    @DisplayName("Получение валюты по ID: валюта не найдена (404)")
    public void getCurrencyByIdNotFound() {
        UUID currencyId = UUID.randomUUID();
        String message = "Валюта с id=" + currencyId + " не найдена";

        Mockito.when(currencyRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.empty());

        ErrorDto expected = ErrorDto.builder()
                .status(HttpStatus.NOT_FOUND)
                .message(message)
                .build();

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/currencies/{currencyId}", currencyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();

        ErrorDto actual = objectMapper.readValue(
                responseContent,
                ErrorDto.class
        );

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND.value());

        Assertions.assertThat(actual.message()).isEqualTo(expected.message());
        Assertions.assertThat(actual.status()).isEqualTo(expected.status());
    }

    @Test
    @SneakyThrows
    @DisplayName("Получение валюты по ID: ошибка 400 (Невалидный формат UUID)")
    public void getCurrencyByIdInvalidUuidFormat() {
        String invalidUuid = "12345-invalid-string";

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/currencies/{currencyId}", invalidUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        Mockito.verifyNoInteractions(currencyRepository);
    }
}