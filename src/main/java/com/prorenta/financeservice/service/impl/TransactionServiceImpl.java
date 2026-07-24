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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final CategoryService categoryService;
    private final CurrencyService currencyService;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public TransactionResponseDto createTransaction(CreateTransactionRequestDto dto) {
        log.info("Инициализация создания транзакции: userId={}", dto.userId());

        UserInfoDto user = new UserInfoDto(dto.userId(), "Name");
        Category category = categoryService.findById(dto.categoryId());
        Currency currency = currencyService.findById(dto.currencyId());

        log.debug("Связанные сущности загружены: categoryId={}, currencyId={}", category.getId(), currency.getId());

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
        log.info("Транзакция успешно сохранена: transactionId={}", saved.getId());

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
