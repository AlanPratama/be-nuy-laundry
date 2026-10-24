package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.AuthUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.util.ResponseUtil;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.auth.LoginRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.auth.RefreshTokenRequest;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.auth.AuthResponseDTO;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.UnauthorizedException;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.UserResponseDTO;
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
    private final String SUCCESS_LOGOUT_MSG = "SUCCESS";
    private final String SUCCESS_RETRIEVE_DATA_MSG = "SUCCESS";

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseUtil.success(SUCCESS_LOGIN_MSG, authUseCase.login(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseUtil.success(SUCCESS_REFRESH_TOKEN_MSG, authUseCase.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String token = extractToken(authHeader);
        authUseCase.logout(token);
        return ResponseUtil.success(SUCCESS_LOGOUT_MSG, null);
    }

    @GetMapping({"/me", "/authenticated-user"})
    public ResponseEntity<ApiResponse<UserResponseDTO>> getAuthenticatedUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String token = extractToken(authHeader);
        return ResponseUtil.success(SUCCESS_RETRIEVE_DATA_MSG, authUseCase.getAuthenticatedUser(token));
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new UnauthorizedException("Token tidak ditemukan");
        }
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }

}