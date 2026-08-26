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

    @Override
    @Transactional(readOnly = true)
    public Category findById(UUID id) {
        return categoryRepository.findById(id).orElseThrow(
                () -> new CategoryNotFoundException("Кактегория с id=" + id + " не найдена")
        );
    }

    @Override
    @Transactional
    public CategoryResponseDto createCategory(CreateCategoryRequestDto dto) {
        log.info("Создание категории: userId={}", dto.userId());

        if (categoryRepository.countLimitByUserId(dto.userId()) > CATEGORY_LIMIT) {
            throw new LimitExceededException("Превышен лимит активных категорий");
        }

        Category category = Category.builder()
                .userId(dto.userId())
                .name(dto.name())
                .type(dto.type())
                .build();

        Category savedCategory = categoryRepository.save(category);
        log.info("Категория успешно сохранена: categoryId={}", savedCategory.getId());
        return categoryMapper.mapCategoryToCategoryResponseDto(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public GetAllCategoriesResponseDto getAllCategoriesByUserId(UUID userId) {
        log.info("Получение списка категорий: userId={}", userId);
        List<Category> categories = categoryRepository.findAllByUserId(userId);
        log.info("Категории успешно найдены: count={}, userId={}", categories.size(), userId);
        return GetAllCategoriesResponseDto.builder()
                .categories(categories.stream()
                        .map(categoryMapper::mapCategoryToCategoryResponseDto)
                        .toList())
                .build();
    }

    @Override
    @Transactional
    public void softRemoveCategory(UUID categoryId) {
        log.info("Удаление категории: categoryId={}", categoryId);
        categoryRepository.softRemoveCategoryById(categoryId);
        log.info("Успешное удаление категории: categoryId={}", categoryId);
    }
}
