package com.be_nuy_laundry.be_nuy_laundry.domain.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSessionModel extends BaseModel {
    private String accessToken;

    private String refreshToken;

    private Boolean isActive;

    private String deviceInfo;

    private LocalDateTime expiresAt;

    private UserModel user;
}
