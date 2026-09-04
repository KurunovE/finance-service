package com.prorenta.financeservice.repository;

import com.prorenta.financeservice.model.entity.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, UUID> {

    Optional<CurrencyRate> findByCurrencyIdAndRateDate(UUID currencyId, LocalDate rateDate);
}
