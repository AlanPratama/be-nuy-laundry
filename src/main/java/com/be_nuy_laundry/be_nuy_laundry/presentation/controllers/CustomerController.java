package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.CustomerUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.response.PageResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.util.PageUtil;
import com.be_nuy_laundry.be_nuy_laundry.common.util.ResponseUtil;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.CustomerModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.CustomerFilter;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.customer.CustomerRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerUseCase customerUseCase;

    private final String SUCCESS_RETRIEVE_DATA_MSG = "SUCCESS";
    private final String SUCCESS_CREATE_DATA_MSG = "SUCCESS";
    private final String SUCCESS_UPDATE_DATA_MSG = "SUCCESS";
    private final String SUCCESS_DELETE_DATA_MSG = "SUCCESS";

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerModel>> create(@Valid @RequestBody CustomerRequestDTO request) {
        CustomerModel customerModel = request.toModel();

        CustomerModel created = customerUseCase.createCustomer(customerModel);
        return ResponseUtil.success(SUCCESS_CREATE_DATA_MSG, created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerModel>> getById(@PathVariable String id) {
        return ResponseUtil.success(SUCCESS_RETRIEVE_DATA_MSG, customerUseCase.getCustomerById(id));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CustomerModel>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address
    ) {
        CustomerFilter customerFilter = CustomerFilter.builder()
                .name(name)
                .phone(phone)
                .address(address)
                .build();

        Pageable pageable = PageUtil.setPageable(page, size, sortBy, direction);

        return ResponseUtil.successPage(SUCCESS_RETRIEVE_DATA_MSG, customerUseCase.getAllCustomers(customerFilter, pageable));
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.customer.CustomerMetricsDTO>> getMetrics() {
        return ResponseUtil.success(SUCCESS_RETRIEVE_DATA_MSG, customerUseCase.getMetrics());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerModel>> update(@PathVariable String id, @Valid @RequestBody CustomerRequestDTO request) {
        CustomerModel customerModel = request.toModel();

        return ResponseUtil.success(SUCCESS_UPDATE_DATA_MSG, customerUseCase.updateCustomer(id, customerModel));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        customerUseCase.deleteCustomer(id);
        return ResponseUtil.success(SUCCESS_DELETE_DATA_MSG, null);
    }
    
}
