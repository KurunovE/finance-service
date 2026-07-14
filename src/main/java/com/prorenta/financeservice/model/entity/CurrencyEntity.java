package com.prorenta.financeservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "currencies")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", nullable = false, length = 3)
    private String code;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

}
