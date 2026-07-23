package com.prorenta.financeservice.model.dto;

import java.util.UUID;

public record UserInfoDto(
        UUID id,
        String name
) {
}
