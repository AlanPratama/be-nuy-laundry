package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.auth;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.BaseModel;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.UserResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserSessionResponseDTO extends BaseModel {

    private String accessToken;

    private String refreshToken;

    private Boolean isActive;

    private String deviceInfo;

    private LocalDateTime expiresAt;

    private UserResponseDTO user;

}
