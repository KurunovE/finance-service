package com.prorenta.financeservice.model.dto;

import com.prorenta.financeservice.model.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateCategoryRequestDto(
        @NotNull(message = "userId обязателен")
        UUID userId,

        @NotBlank(message = "Название категории обязательно")
        @Size(max = 30)
        String name,

        @NotNull(message = "Тип категории обязателен")
        CategoryType type
) {
}
