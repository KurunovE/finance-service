package com.prorenta.financeservice.service;

import com.prorenta.financeservice.model.dto.*;

import java.util.UUID;

public interface TransactionService {

    TransactionResponseDto createTransaction(CreateTransactionRequestDto dto);

    FilterTransactionsResponseDto getTransactions(FilterTransactionRequestDto dto);

    TransactionResponseDto updateTransaction(UUID transactionId, UpdateTransactionRequestDto dto);

    void removeTransaction(UUID transactionId);
}
