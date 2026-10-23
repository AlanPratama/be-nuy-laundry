package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @NotBlank(message = "RefreshToken is required")
    private String refreshToken;

}
