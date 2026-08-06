package com.prorenta.financeservice.util;

import com.prorenta.financeservice.model.entity.Category;
import com.prorenta.financeservice.model.entity.Transaction;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionFilterSpecification {
    public static Specification<Transaction> buildFilter(
            String categoryName,
            LocalDate startCreatedDate,
            LocalDate endCreatedDate
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (categoryName != null && !categoryName.trim().isEmpty()) {
                Join<Transaction, Category> categoryJoin = root.join("category");
                predicates.add(criteriaBuilder.equal(categoryJoin.get("name"), categoryName));
            }

            Optional.ofNullable(startCreatedDate)
                    .map(
                            startDate -> endCreatedDate == null
                                    ? criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate)
                                    : criteriaBuilder.between(root.get("createdAt"), startDate, endCreatedDate)
                    ).ifPresent(predicates::add);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
