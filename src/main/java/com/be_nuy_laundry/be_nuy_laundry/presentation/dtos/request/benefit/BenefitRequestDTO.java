package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.benefit;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.BenefitModel;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BenefitRequestDTO {

    @NotBlank(message = "Nama tidak boleh kosong")
    private String name;

    public BenefitModel toModel() {
        return BenefitModel.builder()
                .name(this.getName())
                .build();
    }

}
