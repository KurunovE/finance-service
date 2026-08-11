package com.prorenta.financeservice.service.impl.unit_tests;

import com.prorenta.financeservice.exception.CategoryNotFoundException;
import com.prorenta.financeservice.exception.LimitExceededException;
import com.prorenta.financeservice.mapper.CategoryMapperImpl;
import com.prorenta.financeservice.model.dto.CategoryResponseDto;
import com.prorenta.financeservice.model.dto.CreateCategoryRequestDto;
import com.prorenta.financeservice.model.dto.GetAllCategoriesResponseDto;
import com.prorenta.financeservice.model.entity.Category;
import com.prorenta.financeservice.repository.CategoryRepository;
import com.prorenta.financeservice.service.CategoryService;
import com.prorenta.financeservice.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.prorenta.financeservice.factory.CategoryDataFactory.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        CategoryServiceImpl.class,
        CategoryMapperImpl.class
})
public class CategoryServiceImplUnitTest {

    @Autowired
    private CategoryService categoryService;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Поиск категории по id: успешно")
    public void findCategoryByIdSuccessfulTest() {
        UUID userId = UUID.randomUUID();
        Category expected = createDefaultCategory(userId);

        Mockito.when(categoryRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.ofNullable(expected));

        Category actual = categoryService.findById(DEFAULT_CATEGORY_ID);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(DEFAULT_CATEGORY_ID, actual.getId());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Поиск категории по id: категория не найдена")
    public void findCategoryByIdCategoryNotFoundTest() {
        String message = "Кактегория с id=" + DEFAULT_CATEGORY_ID + " не найдена";

        Mockito.when(categoryRepository.findById(Mockito.any(UUID.class)))
                .thenThrow(new CategoryNotFoundException(
                        "Кактегория с id=" + DEFAULT_CATEGORY_ID + " не найдена"
                ));

        CategoryNotFoundException thrown = Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.findById(DEFAULT_CATEGORY_ID)
        );

        Assertions.assertEquals(message, thrown.getMessage());
    }

    @Test
    @DisplayName("Создание категории: успешно")
    public void createCategorySuccessfulTest() {
        UUID userId = UUID.randomUUID();
        CreateCategoryRequestDto requestDto = createDefaultCategoryRequestDto(userId);
        Category expectedCategory = createDefaultCategory(userId);

        Mockito.when(categoryRepository.countLimitByUserId(userId))
                .thenReturn(5);
        Mockito.when(categoryRepository.save(Mockito.any(Category.class)))
                .thenReturn(expectedCategory);

        CategoryResponseDto actual = categoryService.createCategory(requestDto);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expectedCategory.getId(), actual.id());
        Assertions.assertEquals(expectedCategory.getName(), actual.name());
        Assertions.assertEquals(expectedCategory.getType(), actual.type());
    }

    @Test
    @DisplayName("Создание категории: ошибка превышения лимита (LimitExceededException)")
    public void createCategoryLimitExceededTest() {
        UUID userId = UUID.randomUUID();
        CreateCategoryRequestDto requestDto = createDefaultCategoryRequestDto(userId);

        Mockito.when(categoryRepository.countLimitByUserId(userId)).thenReturn(31);

        LimitExceededException thrown = Assertions.assertThrows(
                LimitExceededException.class,
                () -> categoryService.createCategory(requestDto)
        );

        Assertions.assertNotNull(thrown.getMessage());

        Mockito.verify(categoryRepository, Mockito.never()).save(Mockito.any(Category.class));
    }

    @Test
    @DisplayName("Получение всех категорий пользователя: успешно (список не пуст)")
    public void getAllCategoriesByUserIdSuccessfulTest() {
        UUID userId = UUID.randomUUID();
        Category category = createDefaultCategory(userId);

        Mockito.when(categoryRepository.findAllByUserId(userId))
                .thenReturn(List.of(category));

        GetAllCategoriesResponseDto actual = categoryService.getAllCategoriesByUserId(userId);

        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.categories());
        Assertions.assertEquals(1, actual.categories().size());
        Assertions.assertEquals(category.getId(), actual.categories().getFirst().id());
    }

    @Test
    @DisplayName("Получение всех категорий пользователя: пустой список")
    public void getAllCategoriesByUserIdEmptyListTest() {
        UUID userId = UUID.randomUUID();

        Mockito.when(categoryRepository.findAllByUserId(userId))
                .thenReturn(List.of());

        GetAllCategoriesResponseDto actual = categoryService.getAllCategoriesByUserId(userId);

        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.categories());
        Assertions.assertTrue(actual.categories().isEmpty());
    }

    @Test
    @DisplayName("Мягкое удаление категории: успешно")
    public void softRemoveCategorySuccessfulTest() {
        UUID categoryId = DEFAULT_CATEGORY_ID;

        categoryService.softRemoveCategory(categoryId);

        Mockito.verify(categoryRepository, Mockito.times(1))
                .softRemoveCategoryById(categoryId);
    }
}
