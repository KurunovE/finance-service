package com.prorenta.financeservice.integration;

import com.prorenta.financeservice.model.dto.UserInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "finance-service",
        url = "${user.service.url}"
)
public interface UserFeignClient {
    @GetMapping("/api/v1/users/{id}")
    ResponseEntity<UserInfoDto> getUserInfo(
            @PathVariable("id") UUID authId
    );
}
