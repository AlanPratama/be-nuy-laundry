package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.adapter;

import com.be_nuy_laundry.be_nuy_laundry.common.exception.DataNotFoundException;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.*;
import com.be_nuy_laundry.be_nuy_laundry.fixture.TestDataBuilder;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.*;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.*;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper.*;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdaptersTest {

    // Customer
    @Mock
    private CustomerJpaRepository customerJpaRepository;
    @InjectMocks
    private CustomerRepositoryAdapter customerRepositoryAdapter;

    // User
    @Mock
    private UserJpaRepository userJpaRepository;
    @InjectMocks
    private UserRepositoryAdapter userRepositoryAdapter;

    // Package
    @Mock
    private PackageJpaRepository packageJpaRepository;
    @Mock
    private BenefitJpaRepository benefitJpaRepository;
    @InjectMocks
    private PackageRepositoryAdapter packageRepositoryAdapter;

    // Benefit
    @InjectMocks
    private BenefitRepositoryAdapter benefitRepositoryAdapter;

    // Transaction
    @Mock
    private TransactionJpaRepository transactionJpaRepository;
    @InjectMocks
    private TransactionRepositoryAdapter transactionRepositoryAdapter;

    // TransactionDetail
    @Mock
    private TransactionDetailJpaRepository transactionDetailJpaRepository;
    @InjectMocks
    private TransactionDetailRepositoryAdapter transactionDetailRepositoryAdapter;

    // TransactionFile
    @Mock
    private TransactionFileJpaRepository transactionFileJpaRepository;
    @InjectMocks
    private TransactionFileRepositoryAdapter transactionFileRepositoryAdapter;

    // Auth
    @Mock
    private UserSessionJpaRepository userSessionJpaRepository;
    @InjectMocks
    private AuthRepositoryAdapter authRepositoryAdapter;

    @Test
    @DisplayName("CustomerRepositoryAdapter - All Methods")
    void testCustomerRepositoryAdapter() {
        CustomerModel customer = TestDataBuilder.createSampleCustomer();
        CustomerEntity entity = CustomerMapper.mapToEntity(customer);

        when(customerJpaRepository.save(any(CustomerEntity.class))).thenReturn(entity);
        when(customerJpaRepository.findById(customer.getId())).thenReturn(Optional.of(entity));
        when(customerJpaRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        CustomerModel saved = customerRepositoryAdapter.save(customer);
        assertThat(saved).isNotNull();
        assertThat(customerRepositoryAdapter.findById(customer.getId())).isPresent();
        assertThat(customerRepositoryAdapter.findAll(CustomerFilter.builder().build(), PageRequest.of(0, 10))).hasSize(1);

        customerRepositoryAdapter.deleteById(customer.getId());
        verify(customerJpaRepository).deleteById(customer.getId());
    }

    @Test
    @DisplayName("UserRepositoryAdapter - All Methods")
    void testUserRepositoryAdapter() {
        UserModel user = TestDataBuilder.createSampleUser();
        UserEntity entity = UserMapper.mapToEntity(user);

        when(userJpaRepository.save(any(UserEntity.class))).thenReturn(entity);
        when(userJpaRepository.findById(user.getId())).thenReturn(Optional.of(entity));
        when(userJpaRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(entity));
        when(userJpaRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        UserModel saved = userRepositoryAdapter.save(user);
        assertThat(saved).isNotNull();
        assertThat(userRepositoryAdapter.findById(user.getId())).isPresent();
        assertThat(userRepositoryAdapter.findByUsername(user.getUsername())).isPresent();
        assertThat(userRepositoryAdapter.findAll(UserFilter.builder().build(), PageRequest.of(0, 10))).hasSize(1);

        userRepositoryAdapter.deleteById(user.getId());
        verify(userJpaRepository).deleteById(user.getId());

        when(userJpaRepository.count()).thenReturn(5L);
        when(userJpaRepository.countByIsActiveTrue()).thenReturn(4L);
        when(userJpaRepository.countByRoleIn(any())).thenReturn(2L);
        when(userJpaRepository.countByRole(any())).thenReturn(3L);
        var metrics = userRepositoryAdapter.getMetrics();
        assertThat(metrics.getTotalStaff()).isEqualTo(5L);
        assertThat(metrics.getActiveStaff()).isEqualTo(4L);
        assertThat(metrics.getInactiveStaff()).isEqualTo(1L);
        assertThat(metrics.getAdminCount()).isEqualTo(2L);
        assertThat(metrics.getCashierCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("PackageRepositoryAdapter - All Methods")
    void testPackageRepositoryAdapter() {
        PackageModel pkg = TestDataBuilder.createSamplePackage();
        pkg.setBenefitIds(List.of("b1"));
        PackageEntity entity = PackageMapper.mapToEntity(pkg);
        BenefitEntity benefitEntity = new BenefitEntity();
        benefitEntity.setId("b1");

        when(benefitJpaRepository.findAllById(List.of("b1"))).thenReturn(List.of(benefitEntity));
        when(packageJpaRepository.save(any(PackageEntity.class))).thenReturn(entity);
        when(packageJpaRepository.findById(pkg.getId())).thenReturn(Optional.of(entity));
        when(packageJpaRepository.findByBenefitId("b1")).thenReturn(List.of(entity));
        when(packageJpaRepository.findByIds(List.of("id1"))).thenReturn(List.of(entity));
        when(packageJpaRepository.findByName("name")).thenReturn(Optional.of(entity));
        when(packageJpaRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        PackageModel saved = packageRepositoryAdapter.save(pkg);
        assertThat(saved).isNotNull();
        assertThat(packageRepositoryAdapter.findById(pkg.getId())).isPresent();
        assertThat(packageRepositoryAdapter.findByBenefitId("b1")).hasSize(1);
        assertThat(packageRepositoryAdapter.findByIds(List.of("id1"))).hasSize(1);
        assertThat(packageRepositoryAdapter.findByName("name")).isPresent();
        assertThat(packageRepositoryAdapter.findAll(PackageFilter.builder().build(), PageRequest.of(0, 10))).hasSize(1);

        packageRepositoryAdapter.deleteById(pkg.getId());
        verify(packageJpaRepository).deleteById(pkg.getId());

        // Mismatched benefit count throws DataNotFoundException
        when(benefitJpaRepository.findAllById(List.of("b1"))).thenReturn(List.of());
        assertThatThrownBy(() -> packageRepositoryAdapter.save(pkg))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("Beberapa benefit ID tidak ditemukan");
    }

    @Test
    @DisplayName("BenefitRepositoryAdapter - All Methods")
    void testBenefitRepositoryAdapter() {
        BenefitModel benefit = TestDataBuilder.createSampleBenefit();
        BenefitEntity entity = BenefitMapper.mapToEntity(benefit);

        when(benefitJpaRepository.save(any(BenefitEntity.class))).thenReturn(entity);
        when(benefitJpaRepository.findById(benefit.getId())).thenReturn(Optional.of(entity));
        when(benefitJpaRepository.findByName(benefit.getName())).thenReturn(Optional.of(entity));
        when(benefitJpaRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        BenefitModel saved = benefitRepositoryAdapter.save(benefit);
        assertThat(saved).isNotNull();
        assertThat(benefitRepositoryAdapter.findById(benefit.getId())).isPresent();
        assertThat(benefitRepositoryAdapter.findByName(benefit.getName())).isPresent();
        assertThat(benefitRepositoryAdapter.findAll(BenefitFilter.builder().build(), PageRequest.of(0, 10))).hasSize(1);

        benefitRepositoryAdapter.deleteById(benefit.getId());
        verify(benefitJpaRepository).deleteById(benefit.getId());
    }

    @Test
    @DisplayName("TransactionRepositoryAdapter - All Methods")
    void testTransactionRepositoryAdapter() {
        TransactionModel trx = TestDataBuilder.createSampleTransaction();
        TransactionEntity entity = TransactionMapper.mapToEntity(trx);

        when(transactionJpaRepository.save(any(TransactionEntity.class))).thenReturn(entity);
        when(transactionJpaRepository.findById(trx.getId())).thenReturn(Optional.of(entity));
        when(transactionJpaRepository.getInvoiceNo(trx.getId())).thenReturn("TRX-123");
        when(transactionJpaRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        TransactionModel saved = transactionRepositoryAdapter.save(trx);
        assertThat(saved).isNotNull();
        assertThat(transactionRepositoryAdapter.findById(trx.getId())).isPresent();
        assertThat(transactionRepositoryAdapter.getInvoiceNo(trx.getId())).isEqualTo("TRX-123");
        assertThat(transactionRepositoryAdapter.findAll(TransactionFilter.builder().build(), PageRequest.of(0, 10))).hasSize(1);

        transactionRepositoryAdapter.deleteById(trx.getId());
        verify(transactionJpaRepository).deleteById(trx.getId());

        when(transactionJpaRepository.count()).thenReturn(5L);
        when(transactionJpaRepository.countByStatus(any())).thenReturn(2L);
        when(transactionJpaRepository.sumAllTotalPrice()).thenReturn(java.math.BigDecimal.valueOf(100000));
        var metrics = transactionRepositoryAdapter.getMetrics();
        assertThat(metrics.getTotalTransactions()).isEqualTo(5L);
        assertThat(metrics.getTotalRevenue()).isEqualTo(java.math.BigDecimal.valueOf(100000));
    }

    @Test
    @DisplayName("TransactionDetailRepositoryAdapter - All Methods")
    void testTransactionDetailRepositoryAdapter() {
        TransactionDetailModel detail = new TransactionDetailModel();
        detail.setId("det-1");
        TransactionDetailEntity entity = TransactionDetailMapper.mapToEntity(detail);

        when(transactionDetailJpaRepository.findByTransactionId("trx-1")).thenReturn(List.of(entity));
        when(transactionDetailJpaRepository.findById("det-1")).thenReturn(Optional.of(entity));

        assertThat(transactionDetailRepositoryAdapter.findByTransactionId("trx-1")).hasSize(1);
        assertThat(transactionDetailRepositoryAdapter.findById("det-1")).isPresent();

        transactionDetailRepositoryAdapter.deleteById("det-1");
        verify(transactionDetailJpaRepository).deleteById("det-1");
    }

    @Test
    @DisplayName("TransactionFileRepositoryAdapter - All Methods")
    void testTransactionFileRepositoryAdapter() {
        TransactionFileModel file = TestDataBuilder.createSampleTransactionFile();
        TransactionFileEntity entity = TransactionFileMapper.mapToEntity(file);

        when(transactionFileJpaRepository.findById(file.getId())).thenReturn(Optional.of(entity));
        when(transactionFileJpaRepository.findAllById(List.of(file.getId()))).thenReturn(List.of(entity));
        when(transactionFileJpaRepository.saveAll(any())).thenReturn(List.of(entity));

        assertThat(transactionFileRepositoryAdapter.findById(file.getId())).isPresent();
        assertThat(transactionFileRepositoryAdapter.findByIds(List.of(file.getId()))).hasSize(1);
        assertThat(transactionFileRepositoryAdapter.saveAll(List.of(file))).hasSize(1);

        transactionFileRepositoryAdapter.deleteById(file.getId());
        verify(transactionFileJpaRepository).deleteById(file.getId());

        transactionFileRepositoryAdapter.deleteByIds(List.of(file.getId()));
        verify(transactionFileJpaRepository).deleteByIds(List.of(file.getId()));
    }

    @Test
    @DisplayName("AuthRepositoryAdapter - All Methods")
    void testAuthRepositoryAdapter() {
        UserSessionModel session = TestDataBuilder.createSampleSession();
        UserSessionEntity entity = UserSessionMapper.mapToEntity(session);

        when(userSessionJpaRepository.findById(session.getId())).thenReturn(Optional.of(entity));
        when(userSessionJpaRepository.findByRefreshToken("token")).thenReturn(Optional.of(entity));
        when(userSessionJpaRepository.save(any(UserSessionEntity.class))).thenReturn(entity);

        assertThat(authRepositoryAdapter.findById(session.getId())).isPresent();
        assertThat(authRepositoryAdapter.findByRefreshToken("token")).isPresent();
        assertThat(authRepositoryAdapter.save(session)).isNotNull();

        authRepositoryAdapter.deactiveSession("sess-1");
        verify(userSessionJpaRepository).deactiveSession("sess-1");

        authRepositoryAdapter.deactiveAllSessionByUserId("u-1");
        verify(userSessionJpaRepository).deactiveAllSessionByUserId("u-1");
    }
}
