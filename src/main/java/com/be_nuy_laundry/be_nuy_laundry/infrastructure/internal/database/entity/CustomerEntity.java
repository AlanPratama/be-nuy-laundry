package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "customers")
@AllArgsConstructor
@NoArgsConstructor
public class CustomerEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String phone;

    private String address;

    @Column(name = "total_cucian_kg", precision = 10, scale = 2)
    private java.math.BigDecimal totalCucianKg = java.math.BigDecimal.ZERO;

    @Column(name = "total_cucian_satuan")
    private Integer totalCucianSatuan = 0;

    @Column(name = "total_transaksi")
    private Integer totalTransaksi = 0;

    @Column(name = "last_order")
    private java.time.LocalDateTime lastOrder;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransactionEntity> transactions;

}

