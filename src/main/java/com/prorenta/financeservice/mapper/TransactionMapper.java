package com.prorenta.financeservice.mapper;

import com.prorenta.financeservice.model.dto.TransactionResponseDto;
import com.prorenta.financeservice.model.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "category", source = "category.name")
    @Mapping(target = "currencyCode", source = "currency.code")
    TransactionResponseDto mapTransactionToTransactionResponseDto(Transaction transaction);
}
