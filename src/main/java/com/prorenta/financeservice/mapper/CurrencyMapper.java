package com.prorenta.financeservice.mapper;

import com.prorenta.financeservice.model.dto.CurrencyResponseDto;
import com.prorenta.financeservice.model.entity.Currency;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CurrencyMapper {
    CurrencyResponseDto mapCurrencyToCurrencyResponseDto(Currency currency);
}
