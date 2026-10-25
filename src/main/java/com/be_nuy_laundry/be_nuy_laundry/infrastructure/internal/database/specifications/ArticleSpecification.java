package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.specifications;

import com.be_nuy_laundry.be_nuy_laundry.common.util.ValueUtil;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.ArticleEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ArticleSpecification {

    public static Specification<ArticleEntity> getFilterSpecification(String search, String category, Boolean isPublished) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (ValueUtil.hasValueString(search)) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate titleMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern);
                Predicate excerptMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("excerpt")), searchPattern);
                predicates.add(criteriaBuilder.or(titleMatch, excerptMatch));
            }

            if (ValueUtil.hasValueString(category) && !"ALL".equalsIgnoreCase(category) && !"SEMUA".equalsIgnoreCase(category)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("category")), category.trim().toLowerCase()));
            }

            if (isPublished != null) {
                predicates.add(criteriaBuilder.equal(root.get("isPublished"), isPublished));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
