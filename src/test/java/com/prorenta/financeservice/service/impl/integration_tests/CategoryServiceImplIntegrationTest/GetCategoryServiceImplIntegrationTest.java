package com.prorenta.financeservice.service.impl.integration_tests.CategoryServiceImplIntegrationTest;

import com.prorenta.financeservice.factory.UserInfoDataFactory;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.prorenta.financeservice.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prorenta.financeservice.model.dto.GetAllCategoriesResponseDto;
import com.prorenta.financeservice.service.impl.integration_tests.AbstractIntegrationTest;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class GetCategoryServiceImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @Test
    @Sql(
            scripts = {
                    "/sql/cleanup.sql",
                    "/sql/insert_category.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @SneakyThrows
    @DisplayName("Получение категории: успешно")
    public void getAllCategoriesByUserIdTest() {
        // Given
        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        // When
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        // Then
        String responseContent = mvcResult.getResponse().getContentAsString();
        GetAllCategoriesResponseDto actual = objectMapper.readValue(
                responseContent,
                GetAllCategoriesResponseDto.class
        );

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.OK.value());

        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.categories()).hasSize(1);
        Assertions.assertThat(actual.categories().getFirst().name()).isEqualTo("Продукты");
    }

    @Test
    @Sql(
            scripts = {
                    "/sql/cleanup.sql",
                    "/sql/insert_category.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @SneakyThrows
    @DisplayName("Получение категории: пустой список")
    public void getAllCategoriesForUnknownUserTest() {
        // Given
        UUID unknownUserId = UUID.randomUUID();

        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(unknownUserId);

        // When
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        // Then
        String responseContent = mvcResult.getResponse().getContentAsString();
        GetAllCategoriesResponseDto actual = objectMapper.readValue(
                responseContent,
                GetAllCategoriesResponseDto.class
        );

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.OK.value());

        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.categories()).isEmpty();
    }

    @Test
    @SneakyThrows
    @DisplayName("Старый маршрут категорий недоступен")
    public void cannotSelectUserThroughLegacyCategoryRoute() {
        // Given
        String invalidUuid = "not-a-valid-uuid";

        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        // When
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/categories/{userId}", invalidUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        // Then
        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value());
    }
}
