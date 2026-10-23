package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.specifications;

import com.be_nuy_laundry.be_nuy_laundry.common.util.ValueUtil;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.CustomerEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.CustomerFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CustomerSpecification {

    public static <T> Specification<CustomerEntity> getFilterSpecification(CustomerFilter customerFilter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (customerFilter != null) {
                if (ValueUtil.hasValueString(customerFilter.getName()))
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + customerFilter.getName().toLowerCase() + "%"));

                if (ValueUtil.hasValueString(customerFilter.getPhone()))
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), "%" + customerFilter.getPhone().toLowerCase() + "%"));

                if (ValueUtil.hasValueString(customerFilter.getAddress()))
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), "%" + customerFilter.getAddress().toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
