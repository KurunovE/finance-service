package com.prorenta.financeservice.model.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CurrencyResponseDto(
        UUID id,
        String code,
        String name
) {
}
