package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.specifications;

import com.be_nuy_laundry.be_nuy_laundry.common.util.ValueUtil;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.UserEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.UserFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<UserEntity> getFilterSpecification(UserFilter userFilter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userFilter != null) {
                if (ValueUtil.hasValueString(userFilter.getName()))
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + userFilter.getName().toLowerCase() + "%"));

                if (ValueUtil.hasValueString(userFilter.getUsername()))
                    predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("username")), userFilter.getUsername().toLowerCase()));

                if (ValueUtil.hasValueString(userFilter.getPhone()))
                    predicates.add(criteriaBuilder.equal(root.get("phone"), userFilter.getPhone()));

                if (ValueUtil.hasValueObject(userFilter.getRole()))
                    predicates.add(criteriaBuilder.equal(root.get("role"), userFilter.getRole()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
