package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.specifications;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.TransactionEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.TransactionFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

    public static Specification<TransactionEntity> getFilterSpecification(TransactionFilter transactionFilter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (transactionFilter != null) {
                if (transactionFilter.getName() != null && !transactionFilter.getName().trim().isEmpty()) {
                    String pattern = "%" + transactionFilter.getName().trim().toLowerCase() + "%";
                    Predicate invoicePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("invoiceNo")), pattern);
                    Predicate customerPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("customerName")), pattern);
                    Predicate phonePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("customerPhone")), pattern);
                    predicates.add(criteriaBuilder.or(invoicePredicate, customerPredicate, phonePredicate));
                }


                if (transactionFilter.getStatus() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), transactionFilter.getStatus()));
                }

                if (transactionFilter.getPaymentStatus() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("paymentStatus"), transactionFilter.getPaymentStatus()));
                }

                if (transactionFilter.getStartDate() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("orderDate"), transactionFilter.getStartDate()));
                }

                if (transactionFilter.getEndDate() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("orderDate"), transactionFilter.getEndDate()));
                }

                if (transactionFilter.getUserId() != null && !transactionFilter.getUserId().trim().isEmpty()) {
                    predicates.add(criteriaBuilder.equal(root.get("user").get("id"), transactionFilter.getUserId().trim()));
                }

                if (transactionFilter.getCustomerId() != null && !transactionFilter.getCustomerId().trim().isEmpty()) {
                    predicates.add(criteriaBuilder.equal(root.get("customer").get("id"), transactionFilter.getCustomerId().trim()));
                }

                if (transactionFilter.getPackageId() != null && !transactionFilter.getPackageId().trim().isEmpty()) {
                    jakarta.persistence.criteria.Join<Object, Object> detailsJoin = root.join("transactionDetails");
                    predicates.add(criteriaBuilder.equal(detailsJoin.get("pkg").get("id"), transactionFilter.getPackageId().trim()));
                    if (query != null) {
                        query.distinct(true);
                    }
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
