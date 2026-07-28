package com.prorenta.financeservice.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateTransactionRequestDto(
        UUID categoryId,
        UUID currencyId,
        BigDecimal amount,
        String bank,
        String description,
        LocalDate createdDate
) {
}
