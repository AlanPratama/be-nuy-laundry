package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.packages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageMetricsDTO {
    private Long totalPackages;
    private Long activePackages;
    private Long weightPackages;
    private Long piecePackages;
}
