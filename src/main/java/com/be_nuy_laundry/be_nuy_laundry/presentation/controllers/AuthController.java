package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.AuthUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.util.ResponseUtil;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.auth.LoginRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.auth.RefreshTokenRequest;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.auth.AuthResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;

    private final String SUCCESS_LOGIN_MSG = "SUCCESS";
    private final String SUCCESS_REFRESH_TOKEN_MSG = "SUCCESS";

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseUtil.success(SUCCESS_LOGIN_MSG, authUseCase.login(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseUtil.success(SUCCESS_REFRESH_TOKEN_MSG, authUseCase.refreshToken(request));
    }

    
}
