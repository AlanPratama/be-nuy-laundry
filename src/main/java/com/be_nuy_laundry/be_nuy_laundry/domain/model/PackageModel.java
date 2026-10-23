package com.be_nuy_laundry.be_nuy_laundry.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PackageType;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PackageModel extends BaseModel {
    private String name;
    private String description;
    private PackageType type;
    private BigDecimal price;
    private Integer daysEstimation;
    private String photo;
    private List<String> benefitIds = new ArrayList<>();
    private List<BenefitModel> benefits = new ArrayList<>();
    private List<TransactionDetailModel> transactionDetails = new ArrayList<>();
}