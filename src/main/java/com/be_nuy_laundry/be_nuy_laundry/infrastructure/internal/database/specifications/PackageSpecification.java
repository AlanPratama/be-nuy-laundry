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
                List<Predicate> searchPredicates = new ArrayList<>();
                if (ValueUtil.hasValueString(packageFilter.getName())) {
                    searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + packageFilter.getName().trim().toLowerCase() + "%"));
                }

                if (ValueUtil.hasValueString(packageFilter.getDescription())) {
                    searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + packageFilter.getDescription().trim().toLowerCase() + "%"));
                }

                if (!searchPredicates.isEmpty()) {
                    predicates.add(criteriaBuilder.or(searchPredicates.toArray(new Predicate[0])));
                }

                if (ValueUtil.hasValueObject(packageFilter.getType()))
                    predicates.add(criteriaBuilder.equal(root.get("type"), packageFilter.getType()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
