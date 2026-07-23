package com.prorenta.financeservice.service.impl;

import com.prorenta.financeservice.mapper.TransactionMapper;
import com.prorenta.financeservice.model.dto.*;
import com.prorenta.financeservice.model.entity.Category;
import com.prorenta.financeservice.model.entity.Currency;
import com.prorenta.financeservice.model.entity.Transaction;
import com.prorenta.financeservice.repository.TransactionRepository;
import com.prorenta.financeservice.service.CategoryService;
import com.prorenta.financeservice.service.CurrencyService;
import com.prorenta.financeservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final CategoryService categoryService;
    private final CurrencyService currencyService;

    private final TransactionRepository transactionRepository;

    private final TransactionMapper transactionMapper;

    @Override
    public TransactionResponseDto createTransaction(CreateTransactionRequestDto dto) {
        UserInfoDto user = new UserInfoDto(dto.userId(), "Name");

        Category category = null;
        Currency currency = null;

        Transaction transaction = Transaction.builder()
                .userId(user.id())
                .category(category)
                .currency(currency)
                .amount(dto.amount())
                .bank(dto.bank())
                .description(dto.description())
                .createdDate(dto.createdDate())
                .build();

        Transaction saved = transactionRepository.save(transaction);

        return transactionMapper.mapTransactionToTransactionResponseDto(saved);
    }

    @Override
    public FilterTransactionsResponseDto getTransactions(FilterTransactionRequestDto dto) {
        return null;
    }

    @Override
    public TransactionResponseDto updateTransaction(UUID transactionId, UpdateTransactionRequestDto dto) {
        return null;
    }

    @Override
    public void removeTransaction(UUID transactionId) {

    }
}
