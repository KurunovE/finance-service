package com.prorenta.financeservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record CurrencyRateDto(

        @Schema(description = "Дата действия курса из ответа ЦБ")
        LocalDate date,

        @Schema(description = "Стоимость указанного номинала валюты в рублях")
        BigDecimal rate,

        @Schema(description = "Количество единиц валюты; null для старых записей без известного номинала")
        Integer nominal
) {
}
