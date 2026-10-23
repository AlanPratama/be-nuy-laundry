package com.be_nuy_laundry.be_nuy_laundry.application.ports.out;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.CustomerModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.CustomerFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CustomerRepositoryPort {
    CustomerModel save(CustomerModel customerModel);
    Optional<CustomerModel> findById(String id);
    Page<CustomerModel> findAll(CustomerFilter customerFilter, Pageable pageable);
    void deleteById(String id);
}
