package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.specifications;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.*;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PackageType;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.UserRole;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.*;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpecificationsTest {

    @Mock
    private Root<CustomerEntity> customerRoot;
    @Mock
    private Root<UserEntity> userRoot;
    @Mock
    private Root<PackageEntity> packageRoot;
    @Mock
    private Root<BenefitEntity> benefitRoot;
    @Mock
    private Root<TransactionEntity> transactionRoot;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<Object> path;

    @Mock
    private Expression<String> stringExpr;

    @Mock
    private Predicate predicate;

    @BeforeEach
    void setUp() {
        lenient().when(cb.and(any(Predicate[].class))).thenReturn(predicate);
        lenient().when(cb.lower(any())).thenReturn(stringExpr);
        lenient().when(cb.like(any(), anyString())).thenReturn(predicate);
        lenient().when(cb.equal(any(), any())).thenReturn(predicate);
    }

    @Test
    @DisplayName("CustomerSpecification - with values and null")
    void testCustomerSpecification() {
        new CustomerSpecification();

        CustomerFilter fullFilter = CustomerFilter.builder()
                .name("John")
                .phone("0812")
                .address("Jakarta")
                .build();

        doReturn(path).when(customerRoot).get(anyString());

        Specification<CustomerEntity> spec = CustomerSpecification.getFilterSpecification(fullFilter);
        Predicate result = spec.toPredicate(customerRoot, query, cb);
        assertThat(result).isEqualTo(predicate);

        // Null filter
        Specification<CustomerEntity> nullSpec = CustomerSpecification.getFilterSpecification(null);
        assertThat(nullSpec.toPredicate(customerRoot, query, cb)).isEqualTo(predicate);
    }

    @Test
    @DisplayName("UserSpecification - with values and null")
    void testUserSpecification() {
        new UserSpecification();

        UserFilter fullFilter = UserFilter.builder()
                .name("Admin")
                .username("adminuser")
                .phone("0813")
                .role(UserRole.ADMIN)
                .build();

        doReturn(path).when(userRoot).get(anyString());

        Specification<UserEntity> spec = UserSpecification.getFilterSpecification(fullFilter);
        Predicate result = spec.toPredicate(userRoot, query, cb);
        assertThat(result).isEqualTo(predicate);

        Specification<UserEntity> nullSpec = UserSpecification.getFilterSpecification(null);
        assertThat(nullSpec.toPredicate(userRoot, query, cb)).isEqualTo(predicate);
    }

    @Test
    @DisplayName("PackageSpecification - with values and null")
    void testPackageSpecification() {
        new PackageSpecification();

        PackageFilter fullFilter = PackageFilter.builder()
                .name("Cuci")
                .description("Kilat")
                .type(PackageType.WEIGHT)
                .build();

        doReturn(path).when(packageRoot).get(anyString());

        Specification<PackageEntity> spec = PackageSpecification.getFilterSpecification(fullFilter);
        Predicate result = spec.toPredicate(packageRoot, query, cb);
        assertThat(result).isEqualTo(predicate);

        Specification<PackageEntity> nullSpec = PackageSpecification.getFilterSpecification(null);
        assertThat(nullSpec.toPredicate(packageRoot, query, cb)).isEqualTo(predicate);
    }

    @Test
    @DisplayName("BenefitSpecification - with values and null")
    void testBenefitSpecification() {
        new BenefitSpecification();

        BenefitFilter fullFilter = BenefitFilter.builder()
                .name("Parfum")
                .build();

        doReturn(path).when(benefitRoot).get(anyString());

        Specification<BenefitEntity> spec = BenefitSpecification.getFilterSpecification(fullFilter);
        Predicate result = spec.toPredicate(benefitRoot, query, cb);
        assertThat(result).isEqualTo(predicate);

        Specification<BenefitEntity> nullSpec = BenefitSpecification.getFilterSpecification(null);
        assertThat(nullSpec.toPredicate(benefitRoot, query, cb)).isEqualTo(predicate);
    }

    @Test
    @DisplayName("TransactionSpecification - basic test")
    void testTransactionSpecification() {
        new TransactionSpecification();

        TransactionFilter filter = TransactionFilter.builder().build();
        Specification<TransactionEntity> spec = TransactionSpecification.getFilterSpecification(filter);

        Predicate result = spec.toPredicate(transactionRoot, query, cb);
        assertThat(result).isEqualTo(predicate);
    }
}
