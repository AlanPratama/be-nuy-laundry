package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.AuthUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.auth.LoginRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.auth.RefreshTokenRequest;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.auth.AuthResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthUseCase authUseCase;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("givenValidLoginRequest_whenLogin_thenReturnAuthResponse")
    void givenValidLoginRequest_whenLogin_thenReturnAuthResponse() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("admin");
        request.setPassword("password123");

        AuthResponseDTO responseDTO = new AuthResponseDTO();
        responseDTO.setAccessToken("access-token");
        responseDTO.setRefreshToken("refresh-token");
        when(authUseCase.login(request)).thenReturn(responseDTO);

        ResponseEntity<ApiResponse<AuthResponseDTO>> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(responseDTO);
        assertThat(response.getBody().getMessage()).isEqualTo("SUCCESS");
        verify(authUseCase).login(request);
    }

    @Test
    @DisplayName("givenValidRefreshTokenRequest_whenRefreshToken_thenReturnAuthResponse")
    void givenValidRefreshTokenRequest_whenRefreshToken_thenReturnAuthResponse() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        AuthResponseDTO responseDTO = new AuthResponseDTO();
        responseDTO.setAccessToken("new-access-token");
        responseDTO.setRefreshToken("new-refresh-token");
        when(authUseCase.refreshToken(request)).thenReturn(responseDTO);

        ResponseEntity<ApiResponse<AuthResponseDTO>> response = authController.refreshToken(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(responseDTO);
        assertThat(response.getBody().getMessage()).isEqualTo("SUCCESS");
        verify(authUseCase).refreshToken(request);
    }

    @Test
    @DisplayName("givenBearerToken_whenLogout_thenReturnSuccess")
    void givenBearerToken_whenLogout_thenReturnSuccess() {
        String authHeader = "Bearer sample.jwt.token";

        ResponseEntity<ApiResponse<Void>> response = authController.logout(authHeader);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("SUCCESS");
        verify(authUseCase).logout("sample.jwt.token");
    }

    @Test
    @DisplayName("givenRawToken_whenLogout_thenReturnSuccess")
    void givenRawToken_whenLogout_thenReturnSuccess() {
        String authHeader = "sample.jwt.token";

        ResponseEntity<ApiResponse<Void>> response = authController.logout(authHeader);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("SUCCESS");
        verify(authUseCase).logout("sample.jwt.token");
    }

    @Test
    @DisplayName("givenNullAuthHeader_whenLogout_thenThrowUnauthorizedException")
    void givenNullAuthHeader_whenLogout_thenThrowUnauthorizedException() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> authController.logout(null))
                .isInstanceOf(com.be_nuy_laundry.be_nuy_laundry.common.exception.UnauthorizedException.class)
                .hasMessage("Token tidak ditemukan");
    }

    @Test
    @DisplayName("givenBlankAuthHeader_whenLogout_thenThrowUnauthorizedException")
    void givenBlankAuthHeader_whenLogout_thenThrowUnauthorizedException() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> authController.logout("   "))
                .isInstanceOf(com.be_nuy_laundry.be_nuy_laundry.common.exception.UnauthorizedException.class)
                .hasMessage("Token tidak ditemukan");
    }

    @Test
    @DisplayName("givenBearerToken_whenGetAuthenticatedUser_thenReturnUserResponse")
    void givenBearerToken_whenGetAuthenticatedUser_thenReturnUserResponse() {
        String authHeader = "Bearer sample.jwt.token";
        com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.UserResponseDTO userResponseDTO =
                new com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.UserResponseDTO();
        userResponseDTO.setUsername("admin");
        when(authUseCase.getAuthenticatedUser("sample.jwt.token")).thenReturn(userResponseDTO);

        ResponseEntity<ApiResponse<com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.UserResponseDTO>> response =
                authController.getAuthenticatedUser(authHeader);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(userResponseDTO);
        assertThat(response.getBody().getMessage()).isEqualTo("SUCCESS");
        verify(authUseCase).getAuthenticatedUser("sample.jwt.token");
    }

    @Test
    @DisplayName("givenNullAuthHeader_whenGetAuthenticatedUser_thenThrowUnauthorizedException")
    void givenNullAuthHeader_whenGetAuthenticatedUser_thenThrowUnauthorizedException() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> authController.getAuthenticatedUser(null))
                .isInstanceOf(com.be_nuy_laundry.be_nuy_laundry.common.exception.UnauthorizedException.class)
                .hasMessage("Token tidak ditemukan");
    }
}
