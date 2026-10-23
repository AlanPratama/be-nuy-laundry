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

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<TransactionEntity> transactions;

}
