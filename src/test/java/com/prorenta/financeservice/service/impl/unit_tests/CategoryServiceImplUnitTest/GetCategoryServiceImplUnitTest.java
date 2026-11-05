package com.prorenta.financeservice.service.impl.unit_tests.CategoryServiceImplUnitTest;

import com.prorenta.financeservice.factory.UserInfoDataFactory;
import com.prorenta.financeservice.security.CurrentUserProvider;
import com.prorenta.financeservice.mapper.CategoryMapperImpl;
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
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                CategoryServiceImpl.class,
                CategoryMapperImpl.class
        }
)
public class GetCategoryServiceImplUnitTest {

    @Autowired
    private CategoryService categoryService;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @Test
    @DisplayName("Получение всех категорий пользователя: успешно (список не пуст)")
    public void getAllCategoriesByUserIdSuccessfulTest() {
        // Given
        UUID userId = UserInfoDataFactory.DEFAULT_USER_ID;
        Category category = createDefaultCategory(userId);

        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);
        Mockito.when(categoryRepository.findAllByUserId(userId))
                .thenReturn(List.of(category));

        // When
        GetAllCategoriesResponseDto actual = categoryService.getCurrentUserCategories();

        // Then
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.categories());
        Assertions.assertEquals(1, actual.categories().size());
        Assertions.assertEquals(category.getId(), actual.categories().getFirst().id());
    }

    @Test
    @DisplayName("Получение всех категорий пользователя: пустой список")
    public void getAllCategoriesByUserIdEmptyListTest() {
        // Given
        UUID userId = UserInfoDataFactory.DEFAULT_USER_ID;

        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);
        Mockito.when(categoryRepository.findAllByUserId(userId))
                .thenReturn(List.of());

        // When
        GetAllCategoriesResponseDto actual = categoryService.getCurrentUserCategories();

        // Then
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.categories());
        Assertions.assertTrue(actual.categories().isEmpty());
    }
}
