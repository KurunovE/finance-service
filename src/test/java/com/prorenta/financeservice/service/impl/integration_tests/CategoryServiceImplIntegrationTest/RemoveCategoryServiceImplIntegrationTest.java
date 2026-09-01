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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class RemoveCategoryServiceImplIntegrationTest extends AbstractIntegrationTest {

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
    @DisplayName("Удаление категории: мягкое удаление и проверка скрытия из БД")
    public void softDeleteCategoryIntegrationTest() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID categoryId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        MvcResult deleteResult = mockMvc.perform(delete("/api/v1/categories/{categoryId}", categoryId))
                .andReturn();

        Assertions.assertThat(deleteResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.NO_CONTENT.value());

        MvcResult getResult = mockMvc.perform(get("/api/v1/categories/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        String responseContent = getResult.getResponse().getContentAsString();
        GetAllCategoriesResponseDto actual = objectMapper.readValue(
                responseContent,
                GetAllCategoriesResponseDto.class
        );

        Assertions.assertThat(getResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.OK.value());

        Assertions.assertThat(actual.categories()).isEmpty();
    }
}
