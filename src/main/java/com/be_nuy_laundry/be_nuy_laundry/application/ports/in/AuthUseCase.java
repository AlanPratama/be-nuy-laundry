package com.be_nuy_laundry.be_nuy_laundry.application.ports.in;

import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.auth.LoginRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.auth.RefreshTokenRequest;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.auth.AuthResponseDTO;

public interface AuthUseCase {

    AuthResponseDTO login(LoginRequestDTO request);

    AuthResponseDTO refreshToken(RefreshTokenRequest request);

    void logout(String accessToken);

    void logoutAll(String userId);

}
