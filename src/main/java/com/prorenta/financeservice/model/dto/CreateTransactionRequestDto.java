package com.prorenta.financeservice.model.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTransactionRequestDto(

        @NotNull(message = "userId обязателен")
        UUID userId,

        @NotNull(message = "categoryId обязателен")
        UUID categoryId,

        @NotNull(message = "currencyId обязателен")
        UUID currencyId,

        @NotNull(message = "Сумма обязательна")
        @DecimalMin(value = "0.01", message = "Сумма должна быть больше нуля")
        BigDecimal amount,

        @Size(max = 50, message = "Название банка не может превышать 50 символов")
        String bank,

        @Size(max = 255, message = "Описание не может превышать 255 символов")
        String description,

        @NotNull(message = "Дата обязательна")
        @PastOrPresent(message = "Дата не может быть в будущем")
        LocalDate createdDate

) {
}
