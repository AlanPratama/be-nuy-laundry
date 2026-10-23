package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "benefits")
@Getter
@Setter
public class BenefitEntity extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany(mappedBy = "benefit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PackageBenefitEntity> packageBenefits;

}
