package com.prorenta.financeservice.controller;

import com.prorenta.financeservice.model.dto.CurrencyResponseDto;
import com.prorenta.financeservice.model.dto.ErrorDto;
import com.prorenta.financeservice.model.dto.ListCurrenciesResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Tag(name = "Currencies", description = "Операции с валютами")
@RequestMapping("api/v1/currencies")
public interface CurrencyController {

    @GetMapping
    @Operation(
            summary = "Получение полного списка валют"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение всего списка валют",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ListCurrenciesResponseDto.class)
                    )
            )
    })
    ResponseEntity<ListCurrenciesResponseDto> getCurrencies();

    @GetMapping("/{currencyId}")
    @Operation(
            summary = "Получение валюты по ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение валюты",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CurrencyResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Валюта не найдена",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorDto.class)
                    )
            )
    })
    ResponseEntity<CurrencyResponseDto> getCurrency(
            @PathVariable UUID currencyId
    );
}
