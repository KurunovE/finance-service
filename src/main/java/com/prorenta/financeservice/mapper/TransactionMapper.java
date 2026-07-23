package com.prorenta.financeservice.mapper;

import com.prorenta.financeservice.model.dto.TransactionResponseDto;
import com.prorenta.financeservice.model.entity.Transaction;
import org.mapstruct.Mapper;

@Mapper
public interface TransactionMapper {
    TransactionResponseDto mapTransactionToTransactionResponseDto(Transaction transaction);
}
