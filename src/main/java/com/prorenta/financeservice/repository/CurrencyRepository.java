package com.prorenta.financeservice.repository;

import com.prorenta.financeservice.model.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, UUID> {

    @Query("""
            SELECT c
            FROM Currency c
            WHERE c.isActive = true
            """)
    List<Currency> findAllActiveCurrencies();

    Optional<Currency> findByCode(String code);
}
