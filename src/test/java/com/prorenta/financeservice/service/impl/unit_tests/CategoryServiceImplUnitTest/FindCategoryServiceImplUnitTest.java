package com.prorenta.financeservice.service.impl.unit_tests.CategoryServiceImplUnitTest;

import com.prorenta.financeservice.factory.UserInfoDataFactory;
import com.prorenta.financeservice.security.CurrentUserProvider;
import com.prorenta.financeservice.exception.CategoryNotFoundException;
import com.prorenta.financeservice.mapper.CategoryMapperImpl;
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

import java.util.Optional;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                CategoryServiceImpl.class,
                CategoryMapperImpl.class
        }
)
public class FindCategoryServiceImplUnitTest {

    @Autowired
    private CategoryService categoryService;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @Test
    @DisplayName("Поиск категории по id: успешно")
    public void findCategoryByIdSuccessfulTest() {
        // Given
        UUID userId = UserInfoDataFactory.DEFAULT_USER_ID;
        Category expected = createDefaultCategory(userId);

        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);
        Mockito.when(categoryRepository.findByIdAndUserIdAndIsDeletedFalse(Mockito.any(UUID.class), Mockito.eq(UserInfoDataFactory.DEFAULT_USER_ID)))
                .thenReturn(Optional.ofNullable(expected));

        // When
        Category actual = categoryService.findAvailableCategoryById(DEFAULT_CATEGORY_ID);

        // Then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(DEFAULT_CATEGORY_ID, actual.getId());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Поиск категории по id: категория не найдена")
    public void findCategoryByIdCategoryNotFoundTest() {
        // Given
        String message = "Категория с id=" + DEFAULT_CATEGORY_ID + " не найдена";

        Mockito.when(currentUserProvider.getCurrentUserId())
                .thenReturn(UserInfoDataFactory.DEFAULT_USER_ID);
        Mockito.when(categoryRepository.findByIdAndUserIdAndIsDeletedFalse(Mockito.any(UUID.class), Mockito.eq(UserInfoDataFactory.DEFAULT_USER_ID)))
                .thenReturn(Optional.empty());

        // When
        CategoryNotFoundException thrown = Assertions.assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.findAvailableCategoryById(DEFAULT_CATEGORY_ID)
        );

        // Then
        Assertions.assertEquals(message, thrown.getMessage());
    }
}
