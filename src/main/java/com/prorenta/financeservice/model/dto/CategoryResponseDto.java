package com.prorenta.financeservice.model.dto;

import com.prorenta.financeservice.model.enums.CategoryType;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CategoryResponseDto(
        UUID id,
        String name,
        CategoryType type
) {
}
