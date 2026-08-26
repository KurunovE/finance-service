package com.prorenta.financeservice.service.impl.unit_tests.CategoryServiceImplUnitTest;

import com.prorenta.financeservice.exception.LimitExceededException;
import com.prorenta.financeservice.mapper.CategoryMapperImpl;
import com.prorenta.financeservice.model.dto.CategoryResponseDto;
import com.prorenta.financeservice.model.dto.CreateCategoryRequestDto;
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

import java.util.UUID;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                CategoryServiceImpl.class,
                CategoryMapperImpl.class
        }
)
public class CreateCategoryServiceImplUnitTest {

    @Autowired
    private CategoryService categoryService;

    @MockitoBean
    private CategoryRepository categoryRepository;

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
    @DisplayName("Создание категории: ошибка превышения лимита")
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
}
