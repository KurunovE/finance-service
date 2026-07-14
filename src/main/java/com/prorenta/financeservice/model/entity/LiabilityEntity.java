package com.prorenta.financeservice.model.entity;

import com.prorenta.financeservice.model.enums.LiabilityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "liabilities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LiabilityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "type", nullable = false)
    private LiabilityType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false, updatable = false)
    private CurrencyEntity currency;

    @Column(name = "initial_amount", nullable = false, updatable = false)
    private BigDecimal initialAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", nullable = false)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

}
