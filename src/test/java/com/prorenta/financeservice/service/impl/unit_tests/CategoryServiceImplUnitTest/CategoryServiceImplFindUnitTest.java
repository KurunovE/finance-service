package com.prorenta.financeservice.service.impl.unit_tests.CategoryServiceImplUnitTest;

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
@ContextConfiguration(classes = {
        CategoryServiceImpl.class,
        CategoryMapperImpl.class
})
public class CategoryServiceImplFindUnitTest {

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
}
