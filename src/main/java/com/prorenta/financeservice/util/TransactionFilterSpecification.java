package com.prorenta.financeservice.util;

import com.prorenta.financeservice.model.entity.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class TransactionFilterSpecification {
    public static Specification<Transaction> buildFilter(
            UUID categoryId,
            LocalDate startCreatedDate,
            LocalDate endCreatedDate
    ) {

    }
}
