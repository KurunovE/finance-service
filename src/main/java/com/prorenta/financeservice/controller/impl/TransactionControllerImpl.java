package com.prorenta.financeservice.controller.impl;

import com.prorenta.financeservice.controller.TransactionController;
import com.prorenta.financeservice.model.dto.*;
import com.prorenta.financeservice.service.TransactionService;
import com.prorenta.financeservice.security.CurrentUserProvider;
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
    private final CurrentUserProvider currentUserProvider;

    @Override
    public ResponseEntity<TransactionResponseDto> createTransaction(CreateTransactionRequestDto request) {
        log.debug("Запрос на создание транзакции: userId={}, categoryId={}, currencyId={}, amount={}, createdDate={}",
                currentUserProvider.getCurrentUserId(), request.categoryId(), request.currencyId(),
                request.amount(), request.createdDate());
        TransactionResponseDto createdTransaction = transactionService.createTransaction(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
    }

    @Override
    public ResponseEntity<FilterTransactionsResponseDto> getTransactions(FilterTransactionRequestDto request) {
        log.debug("Запрос на получение списка транзакций: userId={}, filters={}",
                currentUserProvider.getCurrentUserId(), request);
        FilterTransactionsResponseDto transactions = transactionService.getTransactions(request);

        return ResponseEntity.ok(transactions);
    }

    @Override
    public ResponseEntity<TransactionResponseDto> updateTransaction(UUID transactionId, UpdateTransactionRequestDto request) {
        log.debug("Запрос на обновление транзакции: userId={}, transactionId={}, categoryId={}, currencyId={}, amount={}, createdDate={}",
                currentUserProvider.getCurrentUserId(), transactionId, request.categoryId(),
                request.currencyId(), request.amount(), request.createdDate());
        TransactionResponseDto updatedTransaction = transactionService.updateTransaction(transactionId, request);

        return ResponseEntity.ok(updatedTransaction);
    }

    @Override
    public ResponseEntity<Void> softRemoveTransaction(UUID transactionId) {
        log.debug("Запрос на удаление транзакции: userId={}, transactionId={}",
                currentUserProvider.getCurrentUserId(), transactionId);
        transactionService.softRemoveTransaction(transactionId);

        return ResponseEntity.noContent().build();
    }
}
