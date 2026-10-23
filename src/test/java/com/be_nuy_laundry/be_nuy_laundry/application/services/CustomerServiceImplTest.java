package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.common.exception.DataNotFoundException;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.CustomerModel;
import com.be_nuy_laundry.be_nuy_laundry.fixture.TestDataBuilder;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.adapter.CustomerRepositoryAdapter;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.CustomerFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepositoryAdapter customerRepositoryAdapter;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    @DisplayName("givenCustomerModel_whenCreateCustomer_thenSaveAndReturnCustomer")
    void givenCustomerModel_whenCreateCustomer_thenSaveAndReturnCustomer() {
        CustomerModel customer = TestDataBuilder.createSampleCustomer();
        when(customerRepositoryAdapter.save(customer)).thenReturn(customer);

        CustomerModel result = customerService.createCustomer(customer);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(customer.getId());
        verify(customerRepositoryAdapter).save(customer);
    }

    @Test
    @DisplayName("givenExistingId_whenGetCustomerById_thenReturnCustomer")
    void givenExistingId_whenGetCustomerById_thenReturnCustomer() {
        CustomerModel customer = TestDataBuilder.createSampleCustomer();
        when(customerRepositoryAdapter.findById(customer.getId())).thenReturn(Optional.of(customer));

        CustomerModel result = customerService.getCustomerById(customer.getId());

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(customer.getName());
    }

    @Test
    @DisplayName("givenMissingId_whenGetCustomerById_thenThrowDataNotFoundException")
    void givenMissingId_whenGetCustomerById_thenThrowDataNotFoundException() {
        when(customerRepositoryAdapter.findById("unknown-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerById("unknown-id"))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("Customer tidak ditemukan");
    }

    @Test
    @DisplayName("givenFilterAndPageable_whenGetAllCustomers_thenReturnPagedCustomer")
    void givenFilterAndPageable_whenGetAllCustomers_thenReturnPagedCustomer() {
        CustomerModel customer = TestDataBuilder.createSampleCustomer();
        Page<CustomerModel> page = new PageImpl<>(List.of(customer), PageRequest.of(0, 10), 1);
        CustomerFilter filter = CustomerFilter.builder().build();

        when(customerRepositoryAdapter.findAll(filter, PageRequest.of(0, 10))).thenReturn(page);

        Page<CustomerModel> result = customerService.getAllCustomers(filter, PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("givenValidUpdate_whenUpdateCustomer_thenSaveAndReturnUpdatedCustomer")
    void givenValidUpdate_whenUpdateCustomer_thenSaveAndReturnUpdatedCustomer() {
        CustomerModel existing = TestDataBuilder.createSampleCustomer();
        CustomerModel updateData = new CustomerModel();
        updateData.setName("New Name");
        updateData.setPhone("0819999999");
        updateData.setAddress("New Address");

        when(customerRepositoryAdapter.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(customerRepositoryAdapter.save(any(CustomerModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerModel result = customerService.updateCustomer(existing.getId(), updateData);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getPhone()).isEqualTo("0819999999");
        assertThat(result.getAddress()).isEqualTo("New Address");
    }

    @Test
    @DisplayName("givenExistingId_whenDeleteCustomer_thenDeleteById")
    void givenExistingId_whenDeleteCustomer_thenDeleteById() {
        CustomerModel existing = TestDataBuilder.createSampleCustomer();
        when(customerRepositoryAdapter.findById(existing.getId())).thenReturn(Optional.of(existing));

        customerService.deleteCustomer(existing.getId());

        verify(customerRepositoryAdapter).deleteById(existing.getId());
    }
}
