package com.prorenta.financeservice.util;

import com.prorenta.financeservice.model.entity.Category;
import com.prorenta.financeservice.model.entity.Transaction;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionFilterSpecification {

    public static Specification<Transaction> buildFilter(
            UUID userId,
            String categoryName,
            LocalDate startCreatedDate,
            LocalDate endCreatedDate
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("userId"), userId));

            if (categoryName != null && !categoryName.trim().isEmpty()) {
                Join<Transaction, Category> categoryJoin = root.join("category");
                predicates.add(criteriaBuilder.equal(categoryJoin.get("name"), categoryName));
            }

            if (startCreatedDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), startCreatedDate));
            }

            if (endCreatedDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), endCreatedDate));
            }

            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
