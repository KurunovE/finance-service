package com.prorenta.financeservice.model.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record GetAllCategoriesResponseDto(
        List<CategoryResponseDto> categories
) {
}
