package com.prorenta.financeservice.service.impl.module_tests.CategoryServiceImplModuleTest;

import com.prorenta.financeservice.controller.impl.CategoryControllerImpl;
import com.prorenta.financeservice.exception.GlobalExceptionHandler;
import com.prorenta.financeservice.factory.CategoryDataFactory;
import com.prorenta.financeservice.integration.UserFeignClient;
import com.prorenta.financeservice.mapper.CategoryMapperImpl;
import com.prorenta.financeservice.repository.CategoryRepository;
import com.prorenta.financeservice.service.impl.CategoryServiceImpl;
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

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@WebMvcTest
@ContextConfiguration(
        classes = {
                CategoryControllerImpl.class,
                CategoryServiceImpl.class,
                CategoryMapperImpl.class,
                GlobalExceptionHandler.class
        }
)
public class RemoveCategoryServiceImplModuleTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @MockitoBean
    private UserFeignClient userFeignClient;

    @Test
    @SneakyThrows
    @DisplayName("Мягкое удаление категории: успешно")
    public void softRemoveCategorySuccessfully() {
        UUID categoryId = CategoryDataFactory.DEFAULT_CATEGORY_ID;

        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.NO_CONTENT.value());

        Mockito.verify(categoryRepository, Mockito.times(1)).softRemoveCategoryById(categoryId);
    }

    @Test
    @SneakyThrows
    @DisplayName("Мягкое удаление категории: ошибка 400 (Невалидный формат UUID)")
    public void softRemoveCategoryInvalidUuidFormat() {
        String invalidCategoryId = "invalid-uuid-string";

        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/categories/{id}", invalidCategoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        Mockito.verifyNoInteractions(categoryRepository);
    }
}
