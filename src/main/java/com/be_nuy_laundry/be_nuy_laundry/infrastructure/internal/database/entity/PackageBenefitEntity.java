package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "packages_benefits")
@Getter
@Setter
public class PackageBenefitEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "package_id")
    private PackageEntity pkg;

    @ManyToOne
    @JoinColumn(name = "benefit_id")
    private BenefitEntity benefit;

}
