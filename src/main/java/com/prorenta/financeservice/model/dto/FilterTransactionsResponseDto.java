package com.prorenta.financeservice.model.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record FilterTransactionsResponseDto(
        List<TransactionResponseDto> transactions,
        int pageNumber,
        int elementToPage,
        int countPage,
        long countTransactions
) {
}
