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
        log.info("Запрос на создание транзакции: userId={}", request.userId());
        TransactionResponseDto createdTransaction = transactionService.createTransaction(request);
        log.info("Транзакция успешно создана: transactionId={}, userId={}", createdTransaction.id(), request.userId());
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
        log.info("Запрос на обновление транзакции: transactionId={}", transactionId);
        TransactionResponseDto updatedTransaction = transactionService.updateTransaction(transactionId, request);
        log.info("Транзакция успешно обновлена: transactionId={}", transactionId);
        return ResponseEntity.ok(updatedTransaction);
    }

    @Override
    public ResponseEntity<Void> removeTransaction(UUID transactionId) {
        log.info("Запрос на удаление транзакции: transactionId={}", transactionId);
        transactionService.removeTransaction(transactionId);
        log.info("Транзакция успешно удалена: transactionId={}", transactionId);
        return ResponseEntity.noContent().build();
    }
}
