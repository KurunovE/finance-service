package com.prorenta.financeservice.model.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record TransactionResponseDto(
        UUID id,
        String categoryName,
        String currencyCode,
        BigDecimal amount,
        String bank,
        String description,
        LocalDate createdDate
) {
}
