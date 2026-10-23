package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.CustomerUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.response.PageResponse;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.CustomerModel;
import com.be_nuy_laundry.be_nuy_laundry.fixture.TestDataBuilder;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.CustomerFilter;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.customer.CustomerRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerUseCase customerUseCase;

    @InjectMocks
    private CustomerController customerController;

    @Test
    @DisplayName("givenValidRequest_whenCreateCustomer_thenReturnCustomerModel")
    void givenValidRequest_whenCreateCustomer_thenReturnCustomerModel() {
        CustomerRequestDTO request = new CustomerRequestDTO();
        request.setName("Alice");
        request.setPhone("08123456789");
        request.setAddress("Jl. Sudirman No 1");

        CustomerModel createdModel = TestDataBuilder.createSampleCustomer();
        createdModel.setName("Alice");

        when(customerUseCase.createCustomer(any(CustomerModel.class))).thenReturn(createdModel);

        ResponseEntity<ApiResponse<CustomerModel>> response = customerController.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(createdModel);
        verify(customerUseCase).createCustomer(any(CustomerModel.class));
    }

    @Test
    @DisplayName("givenCustomerId_whenGetById_thenReturnCustomerModel")
    void givenCustomerId_whenGetById_thenReturnCustomerModel() {
        CustomerModel customerModel = TestDataBuilder.createSampleCustomer();
        when(customerUseCase.getCustomerById("cust-1")).thenReturn(customerModel);

        ResponseEntity<ApiResponse<CustomerModel>> response = customerController.getById("cust-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getId()).isEqualTo(customerModel.getId());
        verify(customerUseCase).getCustomerById("cust-1");
    }

    @Test
    @DisplayName("givenFiltersAndPageable_whenGetAll_thenReturnPageResponse")
    void givenFiltersAndPageable_whenGetAll_thenReturnPageResponse() {
        Page<CustomerModel> page = new PageImpl<>(List.of(TestDataBuilder.createSampleCustomer()));

        when(customerUseCase.getAllCustomers(any(CustomerFilter.class), any(Pageable.class))).thenReturn(page);

        ResponseEntity<ApiResponse<PageResponse<CustomerModel>>> response =
                customerController.getAll(0, 10, "createdAt", "desc", "Alice", "0812", "Sudirman");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        ArgumentCaptor<CustomerFilter> filterCaptor = ArgumentCaptor.forClass(CustomerFilter.class);
        verify(customerUseCase).getAllCustomers(filterCaptor.capture(), any(Pageable.class));
        assertThat(filterCaptor.getValue().getName()).isEqualTo("Alice");
        assertThat(filterCaptor.getValue().getPhone()).isEqualTo("0812");
        assertThat(filterCaptor.getValue().getAddress()).isEqualTo("Sudirman");
    }

    @Test
    @DisplayName("givenValidRequest_whenUpdateCustomer_thenReturnUpdatedCustomer")
    void givenValidRequest_whenUpdateCustomer_thenReturnUpdatedCustomer() {
        CustomerRequestDTO request = new CustomerRequestDTO();
        request.setName("Alice Updated");
        request.setPhone("08123456789");
        request.setAddress("Jl. Thamrin No 2");

        CustomerModel updatedModel = TestDataBuilder.createSampleCustomer();
        updatedModel.setName("Alice Updated");
        when(customerUseCase.updateCustomer(eq("cust-1"), any(CustomerModel.class))).thenReturn(updatedModel);

        ResponseEntity<ApiResponse<CustomerModel>> response = customerController.update("cust-1", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getName()).isEqualTo("Alice Updated");
        verify(customerUseCase).updateCustomer(eq("cust-1"), any(CustomerModel.class));
    }

    @Test
    @DisplayName("givenCustomerId_whenDeleteCustomer_thenReturnSuccessResponse")
    void givenCustomerId_whenDeleteCustomer_thenReturnSuccessResponse() {
        doNothing().when(customerUseCase).deleteCustomer("cust-1");

        ResponseEntity<ApiResponse<Void>> response = customerController.delete("cust-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("SUCCESS");
        verify(customerUseCase).deleteCustomer("cust-1");
    }
}
