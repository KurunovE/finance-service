package com.prorenta.financeservice.service.impl.unit_tests.CategoryServiceImplUnitTest;

import com.prorenta.financeservice.mapper.CategoryMapperImpl;
import com.prorenta.financeservice.repository.CategoryRepository;
import com.prorenta.financeservice.service.CategoryService;
import com.prorenta.financeservice.service.impl.CategoryServiceImpl;
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
public class RemoveCategoryServiceImplUnitTest {

    @Autowired
    private CategoryService categoryService;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Мягкое удаление категории: успешно")
    public void softRemoveCategorySuccessfulTest() {
        UUID categoryId = DEFAULT_CATEGORY_ID;

        categoryService.softRemoveCategory(categoryId);

        Mockito.verify(categoryRepository, Mockito.times(1))
                .softRemoveCategoryById(categoryId);
    }
}
