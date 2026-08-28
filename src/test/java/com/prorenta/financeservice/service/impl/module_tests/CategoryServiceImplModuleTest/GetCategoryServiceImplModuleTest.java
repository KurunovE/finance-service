package com.prorenta.financeservice.service.impl.module_tests.CategoryServiceImplModuleTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prorenta.financeservice.controller.impl.CategoryControllerImpl;
import com.prorenta.financeservice.exception.GlobalExceptionHandler;
import com.prorenta.financeservice.factory.CategoryDataFactory;
import com.prorenta.financeservice.integration.UserFeignClient;
import com.prorenta.financeservice.mapper.CategoryMapperImpl;
import com.prorenta.financeservice.model.dto.GetAllCategoriesResponseDto;
import com.prorenta.financeservice.model.entity.Category;
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

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest
@ContextConfiguration(
        classes = {
                CategoryControllerImpl.class,
                CategoryServiceImpl.class,
                CategoryMapperImpl.class,
                GlobalExceptionHandler.class
        }
)
public class GetCategoryServiceImplModuleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @MockitoBean
    private UserFeignClient userFeignClient;

    @Test
    @SneakyThrows
    @DisplayName("Получение заполненого списка категорий: успешно")
    public void getAllCategoriesByUserIdSuccessfully() {
        UUID userId = UUID.randomUUID();
        Category category = CategoryDataFactory.createDefaultCategory(userId);

        Mockito.when(categoryRepository.findAllByUserId(Mockito.any(UUID.class)))
                .thenReturn(List.of(category));

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/categories/" + userId)
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

        Assertions.assertThat(actual)
                .isNotNull();
        Assertions.assertThat(actual.categories())
                .hasSize(1);
        Assertions.assertThat(actual.categories().getFirst().id())
                .isEqualTo(category.getId());

        Mockito.verify(categoryRepository, Mockito.times(1))
                .findAllByUserId(Mockito.any(UUID.class));
    }

    @Test
    @SneakyThrows
    @DisplayName("Получение пустого списка категорий: успешно")
    public void getAllCategoriesByUserIdEmptyList() {
        UUID userId = UUID.randomUUID();
        Mockito.when(categoryRepository.findAllByUserId(Mockito.any(UUID.class)))
                .thenReturn(List.of());

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/categories/" + userId)
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
        Assertions.assertThat(actual)
                .isNotNull();
        Assertions.assertThat(actual.categories())
                .isEmpty();
    }

    @Test
    @SneakyThrows
    @DisplayName("Получение списка категорий: ошибка 400 (Отсутствует обязательный параметр userId)")
    public void getAllCategoriesMissingUserIdParam() {
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/categories/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND.value());

        Mockito.verifyNoInteractions(categoryRepository);
    }

    @Test
    @SneakyThrows
    @DisplayName("Получение списка категорий: ошибка 400 (Невалидный формат UUID)")
    public void getAllCategoriesInvalidUuidFormat() {
        String invalidUuid = "12345-invalid-string";

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/categories/" + invalidUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        Mockito.verifyNoInteractions(categoryRepository);
    }
}
