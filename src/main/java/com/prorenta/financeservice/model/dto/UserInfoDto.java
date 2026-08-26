package com.prorenta.financeservice.model.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserInfoDto(
        UUID id,
        String name
) {
}
