package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.specifications;

import com.be_nuy_laundry.be_nuy_laundry.common.util.ValueUtil;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.PackageEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.PackageFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PackageSpecification {

    public static Specification<PackageEntity> getFilterSpecification(PackageFilter packageFilter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (packageFilter != null) {
                if (ValueUtil.hasValueString(packageFilter.getName()))
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + packageFilter.getName().toLowerCase() + "%"));

                if (ValueUtil.hasValueString(packageFilter.getDescription()))
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + packageFilter.getDescription().toLowerCase() + "%"));

                if (ValueUtil.hasValueObject(packageFilter.getType()))
                    predicates.add(criteriaBuilder.equal(root.get("type"), packageFilter.getType()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
