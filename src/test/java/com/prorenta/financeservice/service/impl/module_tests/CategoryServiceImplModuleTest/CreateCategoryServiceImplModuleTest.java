package com.prorenta.financeservice.service.impl.module_tests.CategoryServiceImplModuleTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prorenta.financeservice.controller.impl.CategoryControllerImpl;
import com.prorenta.financeservice.exception.GlobalExceptionHandler;
import com.prorenta.financeservice.factory.CategoryDataFactory;
import com.prorenta.financeservice.factory.UserInfoDataFactory;
import com.prorenta.financeservice.integration.UserFeignClient;
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
import org.springframework.http.ResponseEntity;
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
    private CategoryRepository categoryRepository;

    @MockitoBean
    private UserFeignClient userFeignClient;

    @Test
    @SneakyThrows
    @DisplayName("Создание категории: успешно")
    public void createCategorySuccessfully() {
        UserInfoDto userInfoDto = UserInfoDataFactory.createDefaultUserInfoDto();
        Category savedCategory = CategoryDataFactory.createDefaultCategory(userInfoDto.id());
        CreateCategoryRequestDto requestDto = CategoryDataFactory.createDefaultCategoryRequestDto(userInfoDto.id());
        CategoryResponseDto expected = CategoryDataFactory.createDefaultCategoryResponseDto(savedCategory.getId());

        Mockito.when(userFeignClient.getUserInfo(Mockito.any(UUID.class)))
                .thenReturn(ResponseEntity.ok(userInfoDto));
        Mockito.when(categoryRepository.countLimitByUserId(Mockito.any(UUID.class)))
                .thenReturn(5);
        Mockito.when(categoryRepository.save(Mockito.any(Category.class)))
                .thenReturn(savedCategory);

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/categories")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

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
        UUID userId = UUID.randomUUID();
        CreateCategoryRequestDto requestDto = CategoryDataFactory.createIncorrectCategoryRequestDto(userId);

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/categories")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        Mockito.verifyNoInteractions(categoryRepository);
    }

    @Test
    @SneakyThrows
    @DisplayName("Создание категории: ошибка превышения лимита")
    public void createCategoryWithLimitExceededException() {
        UUID userId = UUID.randomUUID();
        String message = "Превышен лимит активных категорий";

        CreateCategoryRequestDto requestDto = CategoryDataFactory.createDefaultCategoryRequestDto(userId);

        ErrorDto expected = ErrorDto.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(message)
                .build();

        UserInfoDto userInfo = UserInfoDataFactory.createDefaultUserInfoDto();

        Mockito.when(userFeignClient.getUserInfo(Mockito.any(UUID.class)))
                .thenReturn(ResponseEntity.ok(userInfo));
        Mockito.when(categoryRepository.countLimitByUserId(Mockito.any(UUID.class)))
                .thenReturn(31);

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/categories")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

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
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        Mockito.verifyNoInteractions(categoryRepository);
    }

    @Test
    @SneakyThrows
    @DisplayName("Создание категории: пустое имя категории")
    public void createCategoryBlankName() {
        UUID userId = UUID.randomUUID();
        CreateCategoryRequestDto requestDto = CreateCategoryRequestDto.builder()
                .userId(userId)
                .name("   ")
                .type(CategoryType.EXPENSE)
                .build();

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/categories")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        Mockito.verifyNoInteractions(categoryRepository);
    }

    @Test
    @SneakyThrows
    @DisplayName("Создание категории: невалидное значение Enum")
    public void createCategoryInvalidEnum() {
        String invalidJson = """
                {
                  "userId": "11111111-1111-1111-1111-111111111111",
                  "name": "Еда",
                  "type": "UNKNOWN_TYPE"
                }
                """;

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/categories")
                        .content(invalidJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        Mockito.verifyNoInteractions(categoryRepository);
    }

    @Test
    @SneakyThrows
    @DisplayName("Создание категории: превышение максимальной длины имени")
    public void createCategoryNameTooLong() {
        UUID userId = UUID.randomUUID();
        String tooLongName = "A".repeat(31);

        CreateCategoryRequestDto requestDto = CreateCategoryRequestDto.builder()
                .userId(userId)
                .name(tooLongName)
                .type(CategoryType.EXPENSE)
                .build();

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/categories")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        Assertions.assertThat(mvcResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST.value());

        Mockito.verifyNoInteractions(categoryRepository);
    }
}
