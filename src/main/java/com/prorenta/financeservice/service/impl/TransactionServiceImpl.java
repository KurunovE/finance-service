package com.prorenta.financeservice.service.impl;

import com.prorenta.financeservice.exception.TransactionNotFoundException;
import com.prorenta.financeservice.mapper.TransactionMapper;
import com.prorenta.financeservice.model.dto.*;
import com.prorenta.financeservice.model.entity.Category;
import com.prorenta.financeservice.model.entity.Currency;
import com.prorenta.financeservice.model.entity.Transaction;
import com.prorenta.financeservice.repository.TransactionRepository;
import com.prorenta.financeservice.service.CategoryService;
import com.prorenta.financeservice.service.CurrencyService;
import com.prorenta.financeservice.service.TransactionService;
import com.prorenta.financeservice.util.TransactionFilterSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public TransactionResponseDto createTransaction(CreateTransactionRequestDto dto) {
        log.info("Создание транзакции: userId={}", dto.userId());

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

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Транзакция успешно сохранена: transactionId={}", savedTransaction.getId());

        return transactionMapper.mapTransactionToTransactionResponseDto(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public FilterTransactionsResponseDto getTransactions(FilterTransactionRequestDto dto) {
        log.debug("Получение списка транзакций: filters={}", dto);

        Pageable pageable = PageRequest.of(
                dto.page(),
                dto.pageSize(),
                Sort.by(Sort.Direction.fromString(dto.sortDirection()), dto.fieldSort())
        );

        Specification<Transaction> specification = TransactionFilterSpecification.buildFilter(
                dto.categoryName(),
                dto.startCreatedDate(),
                dto.endCreatedDate()
        );

        Page<Transaction> response = transactionRepository.findAll(specification, pageable);
        log.debug("Найдено {} транзакций. {} страница из {}",
                response.getTotalElements(), response.getNumber(), response.getTotalPages());

        return FilterTransactionsResponseDto.builder()
                .transactions(
                        response.getContent().stream()
                                .map(transactionMapper::mapTransactionToTransactionResponseDto)
                                .toList()
                )
                .pageNumber(response.getPageable().getPageNumber())
                .elementToPage(response.getPageable().getPageSize())
                .countPage(response.getTotalPages())
                .countTransactions(response.getTotalElements())
                .build();
    }

    @Override
    @Transactional
    public TransactionResponseDto updateTransaction(UUID transactionId, UpdateTransactionRequestDto dto) {
        log.info("Обновление транзакции: transactionId={}", transactionId);
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(
                () -> new TransactionNotFoundException("Транзакция с id=" + transactionId + " не найдена")
        );
        if (dto.categoryId() != null) {
            Category category = categoryService.findById(dto.categoryId());
            log.debug("Загружена категория: categoryId={}", category.getId());
            transaction.setCategory(category);
        }
        if (dto.currencyId() != null) {
            Currency currency = currencyService.findById(dto.currencyId());
            log.debug("Загружена валюта: currencyId={}", currency.getId());
            transaction.setCurrency(currency);
        }
        if (dto.amount() != null) {
            transaction.setAmount(dto.amount());
        }
        if (dto.bank() != null) {
            transaction.setBank(dto.bank());
        }
        if (dto.description() != null) {
            transaction.setDescription(dto.description());
        }
        if (dto.createdDate() != null) {
            transaction.setCreatedDate(dto.createdDate());
        }
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Транзакция успешно обновлена: transactionId={}", savedTransaction.getId());

        return transactionMapper.mapTransactionToTransactionResponseDto(savedTransaction);
    }

    @Override
    @Transactional
    public void softRemoveTransaction(UUID transactionId) {
        log.info("Удаление транзакции: transactionId={}", transactionId);
        transactionRepository.softRemoveTransaction(transactionId);
        log.info("Успешное удаление транзакции: transactionId={}", transactionId);
    }
}
