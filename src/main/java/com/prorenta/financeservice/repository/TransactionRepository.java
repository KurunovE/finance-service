package com.prorenta.financeservice.repository;

import com.prorenta.financeservice.model.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.id = :transactionId
                AND t.isDeleted = false
            """)
    Optional<Transaction> findActiveTransactionById(@Param("transactionId") UUID transactionId);

    @Modifying
    @Query("""
            UPDATE Transaction t
            SET t.isDeleted = true
            WHERE t.id = :transactionId
            """)
    void softRemoveTransaction(@Param("transactionId") UUID transactionId);
}
