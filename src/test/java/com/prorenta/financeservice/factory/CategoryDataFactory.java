package com.prorenta.financeservice.factory;

import com.prorenta.financeservice.model.dto.CategoryResponseDto;
import com.prorenta.financeservice.model.dto.CreateCategoryRequestDto;
import com.prorenta.financeservice.model.entity.Category;
import com.prorenta.financeservice.model.enums.CategoryType;

import java.util.UUID;

public class CategoryDataFactory {

    public static final UUID DEFAULT_CATEGORY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final String DEFAULT_CATEGORY_NAME = "Еда";

    public static Category createDefaultCategory(
            UUID userId
    ) {
        return Category.builder()
                .id(DEFAULT_CATEGORY_ID)
                .userId(userId)
                .name(DEFAULT_CATEGORY_NAME)
                .type(CategoryType.EXPENSE)
                .build();
    }

    public static CreateCategoryRequestDto createDefaultCategoryRequestDto(
            UUID userId
    ) {
        return CreateCategoryRequestDto.builder()
                .userId(userId)
                .name(DEFAULT_CATEGORY_NAME)
                .type(CategoryType.EXPENSE)
                .build();
    }

    public static CreateCategoryRequestDto createIncorrectCategoryRequestDto(
            UUID userId
    ) {
        return CreateCategoryRequestDto.builder()
                .userId(userId)
                .name("")
                .type(null)
                .build();
    }

    public static CategoryResponseDto createDefaultCategoryResponseDto(
            UUID categoryId
    ) {
        return CategoryResponseDto.builder()
                .id(categoryId)
                .name(DEFAULT_CATEGORY_NAME)
                .type(CategoryType.EXPENSE)
                .build();
    }
}
