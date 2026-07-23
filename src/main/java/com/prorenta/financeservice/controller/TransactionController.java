package com.prorenta.financeservice.controller;

import com.prorenta.financeservice.model.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Transactions", description = "Операции с транзакциями")
@RequestMapping("api/v1/transactions")
public interface TransactionController {

    @PostMapping
    @Operation(
            summary = "Создание транзакции",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания транзакции",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateTransactionRequestDto.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Пример JSON для запроса",
                                    value = """
                                            {
                                                "userId": "123e4567-e89b-12d3-a456-426614174000",
                                                "categoryId": "987e6543-e21b-12d3-a456-426614174000",
                                                "currencyId": "550e8400-e29b-41d4-a716-446655440000",
                                                "amount": 350.50,
                                                "bank": "Сбербанк",
                                                "description": "Обед",
                                                "createdDate": "2026-07-23"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Успешное создание транзакции",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TransactionResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка в запросе",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorDto.class)
                    )
            )
    })
    ResponseEntity<TransactionResponseDto> createTransaction(
            @Valid @RequestBody CreateTransactionRequestDto request
    );

    @GetMapping
    @Operation(summary = "Получение списка транзакций с фильтрацией")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение списка",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FilterTransactionsResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные параметры фильтрации",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorDto.class)
                    )
            )
    })
    ResponseEntity<FilterTransactionsResponseDto> getTransactions(
            @Parameter(description = "Параметры фильтрации и пагинации")
            @Valid @ModelAttribute FilterTransactionRequestDto request
    );

    @PatchMapping("/{id}")
    @Operation(
            summary = "Частичное обновление транзакции",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Поля для обновления",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateTransactionRequestDto.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Пример частичного обновления",
                                    value = """
                                            {
                                                "amount": 450.00,
                                                "description": "Обед с коллегами"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Транзакция успешно обновлена",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TransactionResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации в запросе",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Транзакция не найдена",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorDto.class)
                    )
            )
    })
    ResponseEntity<TransactionResponseDto> updateTransaction(
            @Parameter(description = "ID транзакции", required = true)
            @PathVariable("id") UUID transactionId,
            @Valid @RequestBody UpdateTransactionRequestDto request
    );

    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление транзакции")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Транзакция успешно удалена"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Транзакция не найдена",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorDto.class)
                    )
            )
    })
    ResponseEntity<Void> removeTransaction(
            @Parameter(description = "ID транзакции", required = true)
            @PathVariable("id") UUID transactionId
    );
}
