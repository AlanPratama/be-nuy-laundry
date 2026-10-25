package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.adapter;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.CustomerRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.CustomerModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.CustomerEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.CustomerFilter;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper.CustomerMapper;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository.CustomerJpaRepository;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.specifications.CustomerSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepositoryPort {

    private final CustomerJpaRepository customerJpaRepository;

    @Override
    public CustomerModel save(CustomerModel customerModel) {
        CustomerEntity entity = CustomerMapper.mapToEntity(customerModel);

        CustomerEntity savedEntity = customerJpaRepository.save(entity);

        return CustomerMapper.mapToDomain(savedEntity);
    }

    @Override
    public Optional<CustomerModel> findById(String id) {
        return customerJpaRepository.findById(id).map(CustomerMapper::mapToDomain);
    }

    @Override
    public Page<CustomerModel> findAll(CustomerFilter customerFilter, Pageable pageable) {
        Specification<CustomerEntity> specification = CustomerSpecification.getFilterSpecification(customerFilter);

        return customerJpaRepository.findAll(specification, pageable).map(CustomerMapper::mapToDomain);
    }

    @Override
    public void deleteById(String id) {
        customerJpaRepository.deleteById(id);
    }

    @Override
    public com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.customer.CustomerMetricsDTO getMetrics() {
        java.time.LocalDateTime startOfMonth = java.time.LocalDate.now().withDayOfMonth(1).atStartOfDay();
        java.time.LocalDateTime nextMonth = startOfMonth.plusMonths(1);

        return com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.customer.CustomerMetricsDTO.builder()
                .totalCustomers(customerJpaRepository.count())
                .newCustomersThisMonth(customerJpaRepository.countCreatedBetween(startOfMonth, nextMonth))
                .activeCustomers(customerJpaRepository.countActiveCustomers())
                .build();
    }
}
