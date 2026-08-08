package com.prorenta.financeservice.factory;

import com.prorenta.financeservice.model.dto.UserInfoDto;

import java.util.UUID;

public class UserInfoDataFactory {

    public static final UUID DEFAULT_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final String DEFAULT_USERNAME = "User";

    public static UserInfoDto createDefaultUserInfoDto() {
        return UserInfoDto.builder()
                .id(DEFAULT_USER_ID)
                .name(DEFAULT_USERNAME)
                .build();
    }
}
