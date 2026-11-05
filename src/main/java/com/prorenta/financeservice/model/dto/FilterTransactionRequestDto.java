package com.prorenta.financeservice.model.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;

import java.time.LocalDate;
import java.util.Set;

@Builder
public record FilterTransactionRequestDto(
        String categoryName,
        LocalDate startCreatedDate,
        LocalDate endCreatedDate,
        @Min(value = 0, message = "Номер страницы не может быть отрицательным")
        Integer page,
        @Min(value = 1, message = "Размер страницы должен быть положительным")
        @Max(value = 100, message = "Размер страницы не может превышать 100")
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

    @AssertTrue(message = "Дата начала поиска не должна быть позже даты окончания")
    public boolean isValidPeriod() {
        if (startCreatedDate == null || endCreatedDate == null) {
            return true;
        }
        return !startCreatedDate.isAfter(endCreatedDate);
    }

    @AssertTrue(message = "Направление сортировки должно быть 'asc' или 'desc'")
    public boolean isValidSortDirection() {
        return "asc".equalsIgnoreCase(sortDirection) || "desc".equalsIgnoreCase(sortDirection);
    }

    @AssertTrue(message = "Недопустимое поле для сортировки")
    public boolean isValidFieldSort() {
        return ALLOWED_SORT_FIELDS.contains(fieldSort);
    }
}
