package com.prorenta.financeservice.service.impl.integration_tests.CategoryServiceImplIntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prorenta.financeservice.factory.UserInfoDataFactory;
import com.prorenta.financeservice.model.dto.CreateCategoryRequestDto;
import com.prorenta.financeservice.model.enums.CategoryType;
import com.prorenta.financeservice.service.impl.integration_tests.AbstractIntegrationTest;
import com.prorenta.financeservice.service.impl.integration_tests.util.UserClientHelper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class CreateCategoryServiceImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserClientHelper userClientHelper;

    @Test
    @Sql(
            scripts = {
                    "/sql/cleanup.sql",
                    "/sql/insert_category.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @SneakyThrows
    @DisplayName("Создание категории: дубликат категории (нарушение UNIQUE constraint БД)")
    public void createCategoryDuplicateNameTest() {
        UUID userId = UserInfoDataFactory.DEFAULT_USER_ID;

        CreateCategoryRequestDto requestDto = CreateCategoryRequestDto.builder()
                .userId(userId)
                .name("Продукты")
                .type(CategoryType.EXPENSE)
                .build();

        userClientHelper.mockUserInfo(userId);

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/categories")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.CONFLICT.value());
    }
}
