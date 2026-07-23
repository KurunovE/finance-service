package com.prorenta.financeservice.service;

import com.prorenta.financeservice.model.dto.*;

import java.util.UUID;

public interface TransactionService {

    TransactionResponseDto createTransaction(CreateTransactionRequestDto request);

    FilterTransactionsResponseDto getTransactions(FilterTransactionRequestDto request);

    TransactionResponseDto updateTransaction(UUID transactionId, UpdateTransactionRequestDto request);

    void removeTransaction(UUID transactionId);
}
