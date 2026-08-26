package com.prorenta.financeservice.model.dto;

import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

@Builder
public record ErrorDto(
        HttpStatus status,
        String message,
        ZonedDateTime zonedDateTime
) {
}
