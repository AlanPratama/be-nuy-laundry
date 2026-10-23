package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PackageType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "packages")
@Getter
@Setter
public class PackageEntity extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private PackageType type;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer daysEstimation;

    private String photo;

    @OneToMany(mappedBy = "pkg", cascade = CascadeType.ALL)
    private List<PackageBenefitEntity> benefits = new ArrayList<>();

    @OneToMany(mappedBy = "pkg", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransactionDetailEntity> transactionDetails;


    public void addBenefit(BenefitEntity benefitEntity) {
        PackageBenefitEntity packageBenefitEntity = new PackageBenefitEntity();
        packageBenefitEntity.setPkg(this);
        packageBenefitEntity.setBenefit(benefitEntity);

        this.benefits.add(packageBenefitEntity);
    }

    public void clearBenefits() {
        this.benefits.clear();
    }

}
