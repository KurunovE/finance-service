package com.prorenta.financeservice.service.impl.integration_tests.CategoryServiceImplIntegrationTest;

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
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/categories/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

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
        UUID unknownUserId = UUID.randomUUID();

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/categories/{userId}", unknownUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

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
    @DisplayName("Получение категории: невалидный ID")
    public void getAllCategoriesInvalidUuidTest() {
        String invalidUuid = "not-a-valid-uuid";

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/categories/{userId}", invalidUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
}
