package com.prorenta.financeservice.controller.impl;

import com.prorenta.financeservice.controller.TransactionController;
import com.prorenta.financeservice.model.dto.*;
import com.prorenta.financeservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TransactionControllerImpl implements TransactionController {

    private final TransactionService transactionService;

    @Override
    public ResponseEntity<TransactionResponseDto> createTransaction(CreateTransactionRequestDto request) {
        log.debug("Запрос на создание транзакции: userId={}", request.userId());
        TransactionResponseDto createdTransaction = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
    }

    @Override
    public ResponseEntity<FilterTransactionsResponseDto> getTransactions(FilterTransactionRequestDto request) {
        log.debug("Запрос на получение списка транзакций: фильтры={}", request);
        FilterTransactionsResponseDto transactions = transactionService.getTransactions(request);
        return ResponseEntity.ok(transactions);
    }

    @Override
    public ResponseEntity<TransactionResponseDto> updateTransaction(UUID transactionId, UpdateTransactionRequestDto request) {
        log.debug("Запрос на обновление транзакции: transactionId={}", transactionId);
        TransactionResponseDto updatedTransaction = transactionService.updateTransaction(transactionId, request);
        return ResponseEntity.ok(updatedTransaction);
    }

    @Override
    public ResponseEntity<Void> softRemoveTransaction(UUID transactionId) {
        log.debug("Запрос на удаление транзакции: transactionId={}", transactionId);
        transactionService.softRemoveTransaction(transactionId);
        return ResponseEntity.noContent().build();
    }
}
