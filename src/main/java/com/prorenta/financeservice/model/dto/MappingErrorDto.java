package com.prorenta.financeservice.model.dto;

import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.Map;

@Builder
public record MappingErrorDto(
        HttpStatus status,
        String message,
        ZonedDateTime zonedDateTime,
        Map<String, String> details
) {
}
