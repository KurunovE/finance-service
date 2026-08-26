package com.prorenta.financeservice.controller.impl;

import com.prorenta.financeservice.controller.CategoryController;
import com.prorenta.financeservice.model.dto.CategoryResponseDto;
import com.prorenta.financeservice.model.dto.CreateCategoryRequestDto;
import com.prorenta.financeservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CategoryControllerImpl implements CategoryController {

    private final CategoryService categoryService;

    @Override
    public ResponseEntity<CategoryResponseDto> createCategory(CreateCategoryRequestDto request) {
        log.debug("Создание категории: userId={}", request.userId());
        CategoryResponseDto category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }
}
