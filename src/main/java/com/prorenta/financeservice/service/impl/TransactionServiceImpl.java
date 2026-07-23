package com.prorenta.financeservice.service.impl;

import com.prorenta.financeservice.model.dto.*;
import com.prorenta.financeservice.service.TransactionService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {
    @Override
    public TransactionResponseDto createTransaction(CreateTransactionRequestDto request) {
        return null;
    }

    @Override
    public FilterTransactionsResponseDto getTransactions(FilterTransactionRequestDto request) {
        return null;
    }

    @Override
    public TransactionResponseDto updateTransaction(UUID transactionId, UpdateTransactionRequestDto request) {
        return null;
    }

    @Override
    public void removeTransaction(UUID transactionId) {

    }
}
