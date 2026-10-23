package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.specifications;

import com.be_nuy_laundry.be_nuy_laundry.common.util.ValueUtil;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.BenefitEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.BenefitFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BenefitSpecification {

    public static Specification<BenefitEntity> getFilterSpecification(BenefitFilter benefitFilter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (benefitFilter != null) {
                if (ValueUtil.hasValueString(benefitFilter.getName()))
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + benefitFilter.getName().toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
