package com.prorenta.financeservice.service.impl;

import com.prorenta.financeservice.exception.CategoryNotFoundException;
import com.prorenta.financeservice.exception.LimitExceededException;
import com.prorenta.financeservice.mapper.CategoryMapper;
import com.prorenta.financeservice.model.dto.CategoryResponseDto;
import com.prorenta.financeservice.model.dto.CreateCategoryRequestDto;
import com.prorenta.financeservice.model.dto.GetAllCategoriesResponseDto;
import com.prorenta.financeservice.model.entity.Category;
import com.prorenta.financeservice.repository.CategoryRepository;
import com.prorenta.financeservice.service.CategoryService;
import com.prorenta.financeservice.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final int CATEGORY_LIMIT = 30;

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public Category findAvailableCategoryById(UUID id) {
        UUID userId = currentUserProvider.getCurrentUserId();

        log.debug("Получение категории: categoryId={}, userId={}", id, userId);

        return categoryRepository.findByIdAndUserIdAndIsDeletedFalse(id, userId).orElseThrow(
                () -> new CategoryNotFoundException("Категория с id=" + id + " не найдена")
        );
    }

    @Override
    @Transactional
    public CategoryResponseDto createCategory(CreateCategoryRequestDto dto) {
        UUID userId = currentUserProvider.getCurrentUserId();

        log.debug("Создание категории: userId={}", userId);

        if (categoryRepository.countLimitByUserId(userId) >= CATEGORY_LIMIT) {
            throw new LimitExceededException("Превышен лимит активных категорий");
        }

        Category category = Category.builder()
                .userId(userId)
                .name(dto.name())
                .type(dto.type())
                .build();

        Category savedCategory = categoryRepository.save(category);

        log.info("Категория успешно сохранена: categoryId={}, userId={}", savedCategory.getId(), userId);

        return categoryMapper.mapCategoryToCategoryResponseDto(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public GetAllCategoriesResponseDto getCurrentUserCategories() {
        UUID userId = currentUserProvider.getCurrentUserId();

        log.debug("Получение списка категорий: userId={}", userId);

        List<Category> categories = categoryRepository.findAllByUserId(userId);

        log.debug("Категории успешно найдены: count={}, userId={}", categories.size(), userId);

        return GetAllCategoriesResponseDto.builder()
                .categories(categories.stream()
                        .map(categoryMapper::mapCategoryToCategoryResponseDto)
                        .toList())
                .build();
    }

    @Override
    @Transactional
    public void softRemoveCategory(UUID categoryId) {
        UUID userId = currentUserProvider.getCurrentUserId();

        log.debug("Удаление категории: categoryId={}, userId={}", categoryId, userId);

        if (categoryRepository.softRemoveCategoryById(categoryId, userId) == 0) {
            throw new CategoryNotFoundException("Категория с id=" + categoryId + " не найдена");
        }

        log.info("Успешное удаление категории: categoryId={}, userId={}", categoryId, userId);
    }
}
