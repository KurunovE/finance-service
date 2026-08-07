package com.prorenta.financeservice.model.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.Builder;

import java.time.LocalDate;
import java.util.Set;

@Builder
public record FilterTransactionRequestDto(
        String categoryName,
        LocalDate startCreatedDate,
        LocalDate endCreatedDate,
        Integer page,
        Integer pageSize,
        String sortDirection,
        String fieldSort
) {
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("categoryId", "createdDate", "amount");

    public FilterTransactionRequestDto {
        if (page == null) page = 0;
        if (pageSize == null) pageSize = 10;
        if (sortDirection == null) sortDirection = "asc";
        if (fieldSort == null) fieldSort = "createdDate";
    }

    @AssertTrue(message = "Дата начала поиска должна быть раньше даты окончания")
    public boolean isValidPeriod() {
        if (startCreatedDate == null || endCreatedDate == null) {
            return true;
        }
        return !startCreatedDate.isAfter(endCreatedDate);
    }

    @AssertTrue(message = "Направление сортировки должнобыть 'asc' или 'desc'")
    public boolean isValidSortDirection() {
        return "asc".equalsIgnoreCase(sortDirection) || "desc".equalsIgnoreCase(sortDirection);
    }

    @AssertTrue(message = "Недопустимое поле для сортировки")
    public boolean isValidFieldSort() {
        return ALLOWED_SORT_FIELDS.contains(fieldSort);
    }
}
