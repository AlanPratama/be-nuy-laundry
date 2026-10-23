package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.customer;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.CustomerModel;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerRequestDTO {

    @NotBlank(message = "Nama tidak boleh kosong")
    private String name;
    private String phone;
    private String address;

    public CustomerModel toModel() {
        return CustomerModel.builder()
                .name(this.getName())
                .phone(this.getPhone())
                .address(this.getAddress())
                .build();
    }

}
