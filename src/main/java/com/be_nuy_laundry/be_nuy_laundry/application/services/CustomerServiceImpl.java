package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.CustomerUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.DataNotFoundException;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.CustomerModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.adapter.CustomerRepositoryAdapter;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.CustomerFilter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerUseCase {

    private final CustomerRepositoryAdapter customerRepositoryAdapter;

    @Override
    @Transactional
    public CustomerModel createCustomer(CustomerModel customerModel) {
        return customerRepositoryAdapter.save(customerModel);
    }

    @Override
    public CustomerModel getCustomerById(String id) {
        return customerRepositoryAdapter.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Customer tidak ditemukan"));
    }

    @Override
    public Page<CustomerModel> getAllCustomers(CustomerFilter customerFilter, Pageable pageable) {
        return customerRepositoryAdapter.findAll(customerFilter, pageable);
    }

    @Override
    @Transactional
    public CustomerModel updateCustomer(String id, CustomerModel customerModel) {
        CustomerModel existingCustomerModel = this.getCustomerById(id);
        existingCustomerModel.setName(customerModel.getName());
        existingCustomerModel.setPhone(customerModel.getPhone());
        existingCustomerModel.setAddress(customerModel.getAddress());

        return customerRepositoryAdapter.save(existingCustomerModel);
    }

    @Override
    @Transactional
    public void deleteCustomer(String id) {
        this.getCustomerById(id);
        customerRepositoryAdapter.deleteById(id);
    }

    @Override
    public com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.customer.CustomerMetricsDTO getMetrics() {
        return customerRepositoryAdapter.getMetrics();
    }
}
