package com.prorenta.financeservice.integration;

import com.prorenta.financeservice.config.CbrFeignConfig;
import com.prorenta.financeservice.model.dto.ValCursResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "cbr-client",
        url = "${cbr.exchange-rates.url}",
        configuration = CbrFeignConfig.class
)
public interface CbrFeignClient {

    @GetMapping(value = "${cbr.exchange-rates.value}", consumes = "application/xml")
    ValCursResponseDto getDailyRates(
            @RequestParam(value = "date_req", required = false) String dateReq
    );
}
