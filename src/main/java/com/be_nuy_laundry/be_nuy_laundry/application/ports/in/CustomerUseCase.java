package com.be_nuy_laundry.be_nuy_laundry.application.ports.in;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.CustomerModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.CustomerFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerUseCase {
    CustomerModel createCustomer(CustomerModel customerModel);
    CustomerModel getCustomerById(String id);
    Page<CustomerModel> getAllCustomers(CustomerFilter customerFilter, Pageable pageable);
    CustomerModel updateCustomer(String id, CustomerModel customerModel);
    void deleteCustomer(String id);
}
