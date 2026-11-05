package com.prorenta.financeservice.service.impl.module_tests.CategoryServiceImplModuleTest;

import com.prorenta.financeservice.security.CurrentUserProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prorenta.financeservice.controller.impl.CategoryControllerImpl;
import com.prorenta.financeservice.exception.GlobalExceptionHandler;
import com.prorenta.financeservice.factory.CategoryDataFactory;
import com.prorenta.financeservice.factory.UserInfoDataFactory;
import com.prorenta.financeservice.mapper.CategoryMapperImpl;
import com.prorenta.financeservice.model.dto.CategoryResponseDto;
import com.prorenta.financeservice.model.dto.CreateCategoryRequestDto;
import com.prorenta.financeservice.model.dto.ErrorDto;
import com.prorenta.financeservice.model.dto.UserInfoDto;
import com.prorenta.financeservice.model.entity.Category;
import com.prorenta.financeservice.model.enums.CategoryType;
import com.prorenta.financeservice.repository.CategoryRepository;
import com.prorenta.financeservice.service.impl.CategoryServiceImpl;
import lombok.SneakyThrows;
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
import org.assertj.core.api.Assertions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.ZonedDateTime;
import java.util.UUID;

@WebMvcTest
@ContextConfiguration(
        classes = {
                CategoryControllerImpl.class,
                CategoryServiceImpl.class,
                CategoryMapperImpl.class,
                GlobalExceptionHandler.class
        }
)
public class CreateCategoryServiceImplModuleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @Test
    @SneakyThrows
    @DisplayName("Создание категории: успешно")
    public void createCategorySuccessfully() {
        // Given
        UserInfoDto userInfoDto = UserInfoDataFactory.createDefaultUserInfoDto();
        Category savedCategory = CategoryDataFactory.createDefaultCategory(userInfoDto.userId());
        CreateCategoryRequestDto requestDto = CategoryDataFactory.createDefaultCategoryRequestDto();
        CategoryResponseDto expected = CategoryDataFactory.createDefaultCategoryResponseDto(savedCategory.getId());

        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);
        Mockito.when(categoryRepository.countLimitByUserId(Mockito.any(UUID.class)))
                .thenReturn(5);
        Mockito.when(categoryRepository.save(Mockito.any(Category.class)))
                .thenReturn(savedCategory);

        // When
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/categories")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        // Then
        String responseContent = mvcResult.getResponse().getContentAsString();

        CategoryResponseDto actual = objectMapper.readValue(
                responseContent,
                CategoryResponseDto.class
        );

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.CREATED.value());
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .withEqualsForType(ZonedDateTime::isEqual,ZonedDateTime.class)
                .isEqualTo(expected);
    }

    @Test
    @SneakyThrows
    @DisplayName("Создание категории: ошибка валидации DTO")
    public void createCategoryWithValidationException() {
        // Given
        CreateCategoryRequestDto requestDto = CategoryDataFactory.createIncorrectCategoryRequestDto();

        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        // When
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/categories")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        // Then
        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        Mockito.verifyNoInteractions(categoryRepository);
    }

    @Test
    @SneakyThrows
    @DisplayName("Создание категории: ошибка превышения лимита")
    public void createCategoryWithLimitExceededException() {
        // Given
        String message = "Превышен лимит активных категорий";

        CreateCategoryRequestDto requestDto = CategoryDataFactory.createDefaultCategoryRequestDto();

        ErrorDto expected = ErrorDto.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(message)
                .build();

        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);
        Mockito.when(categoryRepository.countLimitByUserId(Mockito.any(UUID.class)))
                .thenReturn(31);

        // When
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/categories")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        // Then
        String responseContent = mvcResult.getResponse().getContentAsString();

        ErrorDto actual = objectMapper.readValue(
                responseContent,
                ErrorDto.class
        );

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());
        Assertions.assertThat(actual.message())
                .isEqualTo(expected.message());
    }

    @Test
    @SneakyThrows
    @DisplayName("Создание категории: отсутствует тело запроса")
    public void createCategoryMissingBody() {
        // Given
        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        // When
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        // Then
        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        Mockito.verifyNoInteractions(categoryRepository);
    }

    @Test
    @SneakyThrows
    @DisplayName("Создание категории: пустое имя категории")
    public void createCategoryBlankName() {
        // Given
        CreateCategoryRequestDto requestDto = CreateCategoryRequestDto.builder()
                .name("   ")
                .type(CategoryType.EXPENSE)
                .build();

        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        // When
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/categories")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        // Then
        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        Mockito.verifyNoInteractions(categoryRepository);
    }

    @Test
    @SneakyThrows
    @DisplayName("Создание категории: невалидное значение Enum")
    public void createCategoryInvalidEnum() {
        // Given
        String invalidJson = """
                {
                  "userId": "11111111-1111-1111-1111-111111111111",
                  "name": "Еда",
                  "type": "UNKNOWN_TYPE"
                }
                """;

        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        // When
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/categories")
                        .content(invalidJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        // Then
        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        Mockito.verifyNoInteractions(categoryRepository);
    }

    @Test
    @SneakyThrows
    @DisplayName("Создание категории: превышение максимальной длины имени")
    public void createCategoryNameTooLong() {
        // Given
        String tooLongName = "A".repeat(31);

        CreateCategoryRequestDto requestDto = CreateCategoryRequestDto.builder()
                .name(tooLongName)
                .type(CategoryType.EXPENSE)
                .build();

        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);

        // When
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/categories")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        // Then
        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        Mockito.verifyNoInteractions(categoryRepository);
    }
}
