package com.prorenta.financeservice.controller.impl;

import com.prorenta.financeservice.controller.CategoryController;
import com.prorenta.financeservice.model.dto.CategoryResponseDto;
import com.prorenta.financeservice.model.dto.CreateCategoryRequestDto;
import com.prorenta.financeservice.model.dto.GetAllCategoriesResponseDto;
import com.prorenta.financeservice.service.CategoryService;
import com.prorenta.financeservice.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CategoryControllerImpl implements CategoryController {

    private final CategoryService categoryService;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public ResponseEntity<CategoryResponseDto> createCategory(CreateCategoryRequestDto request) {
        log.debug("Запрос на создание категории: userId={}, name={}, type={}",
                currentUserProvider.getCurrentUserId(), request.name(), request.type());
        CategoryResponseDto category = categoryService.createCategory(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @Override
    public ResponseEntity<GetAllCategoriesResponseDto> getCategories() {
        log.debug("Запрос на получение категорий: userId={}", currentUserProvider.getCurrentUserId());

        GetAllCategoriesResponseDto categories = categoryService.getCurrentUserCategories();

        return ResponseEntity.ok(categories);
    }

    @Override
    public ResponseEntity<Void> softRemoveCategory(UUID categoryId) {
        log.debug("Запрос на удаление категории: userId={}, categoryId={}",
                currentUserProvider.getCurrentUserId(), categoryId);
        categoryService.softRemoveCategory(categoryId);

        return ResponseEntity.noContent().build();
    }
}
