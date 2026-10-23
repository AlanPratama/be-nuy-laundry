package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.pkg;

import java.math.BigDecimal;
import java.util.List;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.PackageModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PackageType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PackageRequestDTO {

    @NotBlank(message = "Nama tidak boleh kosong")
    private String name;

    @NotBlank(message = "Deskripsi tidak boleh kosong")
    private String description;

    @NotNull(message = "Tipe tidak boleh kosong")
    private PackageType type;

    @NotNull(message = "Harga tidak boleh kosong")
    @Min(value = 0, message = "Minimal harga 0 rupiah")
    private BigDecimal price;

    @NotNull(message = "Estimasi hari tidak boleh kosong")
    @Min(value = 1, message = "Minimal estimasi 1 hari")
    private Integer daysEstimation;

    @NotNull(message = "Benefit tidak boleh kosong")
    @Size(min = 1, message = "Minimal pilih 1 benefit untuk paket ini")
    List<String> benefitIds;

    @JsonIgnore
    MultipartFile file;

    public PackageModel toModel() {
        return PackageModel.builder()
                .name(this.getName())
                .description(this.getDescription())
                .type(this.getType())
                .price(this.getPrice())
                .daysEstimation(this.getDaysEstimation())
                .benefitIds(this.getBenefitIds())
                .build();
    }
}
