package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponseDTO {
    private String accessToken;
    private String refreshToken;
    private String deviceInfo;
}
