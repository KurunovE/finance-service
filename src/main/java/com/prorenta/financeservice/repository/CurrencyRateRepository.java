package com.prorenta.financeservice.repository;

import com.prorenta.financeservice.model.dto.CurrencyRateDto;
import com.prorenta.financeservice.model.entity.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, UUID> {

    Optional<CurrencyRate> findByCurrencyIdAndRateDate(UUID currencyId, LocalDate rateDate);

    @Query("""
            SELECT new com.prorenta.financeservice.model.dto.CurrencyRateDto(cr.rateDate, cr.rate, cr.nominal)
            FROM CurrencyRate cr
            WHERE cr.currencyId = :currencyId
                AND cr.rateDate >= :startDate
                AND cr.rateDate <= :endDate
            ORDER BY cr.rateDate ASC
            """)
    List<CurrencyRateDto> findAllByCurrencyIdAndRateDateBetweenOrderByRateDateAsc(
            @Param("currencyId") UUID currencyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
