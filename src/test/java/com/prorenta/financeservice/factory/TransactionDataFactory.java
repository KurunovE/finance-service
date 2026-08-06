package com.prorenta.financeservice.factory;

import com.prorenta.financeservice.model.dto.CreateTransactionRequestDto;
import com.prorenta.financeservice.model.dto.TransactionResponseDto;
import com.prorenta.financeservice.model.entity.Category;
import com.prorenta.financeservice.model.entity.Currency;
import com.prorenta.financeservice.model.entity.Transaction;
import com.prorenta.financeservice.model.enums.CategoryType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class TransactionDataFactory {

    public static final UUID DEFAULT_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final LocalDate DEFAULT_DATE = LocalDate.now();

    public static Category createDefaultCategory(UUID userId) {
        return Category.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .name("Category")
                .type(CategoryType.EXPENSE)
                .build();
    }

    public static Currency createDefaultCurrency() {
        return Currency.builder()
                .id(UUID.randomUUID())
                .code("RUB")
                .name("Ruble")
                .build();
    }

    public static Transaction createDefaultTransaction(UUID userId, Category category, Currency currency) {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .category(category)
                .currency(currency)
                .amount(BigDecimal.TEN)
                .bank("Сбербанк")
                .description("Обед")
                .createdDate(DEFAULT_DATE)
                .build();
    }

    public static CreateTransactionRequestDto createRequestDto(UUID userId, Category category, Currency currency) {
        return CreateTransactionRequestDto.builder()
                .userId(userId)
                .categoryId(category.getId())
                .currencyId(currency.getId())
                .amount(BigDecimal.TEN)
                .bank("Сбербанк")
                .description("Обед")
                .createdDate(DEFAULT_DATE)
                .build();
    }

    public static TransactionResponseDto createResponseDto(Transaction transaction, Category category, Currency currency) {
        return TransactionResponseDto.builder()
                .id(transaction.getId())
                .categoryName(category.getName())
                .currencyCode(currency.getCode())
                .amount(transaction.getAmount())
                .bank(transaction.getBank())
                .description(transaction.getDescription())
                .createdDate(transaction.getCreatedDate())
                .build();
    }

}
