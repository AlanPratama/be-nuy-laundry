package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.UserUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.response.PageResponse;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.UserRole;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.UserFilter;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.user.UserCreateRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.user.UserResetPasswordRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.user.UserUpdateRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.UserResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserUseCase userUseCase;

    @InjectMocks
    private UserController userController;

    @Test
    @DisplayName("givenValidRequest_whenCreateUser_thenReturnSuccessResponse")
    void givenValidRequest_whenCreateUser_thenReturnSuccessResponse() {
        UserCreateRequestDTO request = new UserCreateRequestDTO();
        request.setName("John Doe");
        request.setUsername("johndoe");
        request.setPassword("Password123!");
        request.setPhone("08123456789");
        request.setRole(UserRole.STAFF);

        MultipartFile file = new MockMultipartFile("file", "test.png", "image/png", new byte[]{1, 2});
        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setId("user-1");
        responseDTO.setName("John Doe");

        when(userUseCase.createUser(request)).thenReturn(responseDTO);

        ResponseEntity<ApiResponse<UserResponseDTO>> response = userController.create(request, file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(responseDTO);
        assertThat(request.getFile()).isEqualTo(file);
        verify(userUseCase).createUser(request);
    }

    @Test
    @DisplayName("givenUserId_whenGetById_thenReturnUserResponse")
    void givenUserId_whenGetById_thenReturnUserResponse() {
        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setId("user-1");
        when(userUseCase.getUserById("user-1")).thenReturn(responseDTO);

        ResponseEntity<ApiResponse<UserResponseDTO>> response = userController.getById("user-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getId()).isEqualTo("user-1");
        verify(userUseCase).getUserById("user-1");
    }

    @Test
    @DisplayName("givenPaginationParams_whenGetAll_thenReturnPageResponse")
    void givenPaginationParams_whenGetAll_thenReturnPageResponse() {
        Page<UserResponseDTO> page = new PageImpl<>(List.of(new UserResponseDTO()));

        when(userUseCase.getAllUsers(any(UserFilter.class), any(Pageable.class))).thenReturn(page);

        ResponseEntity<ApiResponse<PageResponse<UserResponseDTO>>> response =
                userController.getAll(0, 10, "name", "asc", "John", null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getContent()).hasSize(1);

        ArgumentCaptor<UserFilter> filterCaptor = ArgumentCaptor.forClass(UserFilter.class);
        verify(userUseCase).getAllUsers(filterCaptor.capture(), any(Pageable.class));
        assertThat(filterCaptor.getValue().getName()).isEqualTo("John");
    }

    @Test
    @DisplayName("givenValidRequest_whenUpdateUser_thenReturnUpdatedResponse")
    void givenValidRequest_whenUpdateUser_thenReturnUpdatedResponse() {
        UserUpdateRequestDTO request = new UserUpdateRequestDTO();
        request.setName("Updated Name");
        MultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", new byte[]{3, 4});

        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setId("user-1");
        responseDTO.setName("Updated Name");

        when(userUseCase.updateUser(eq("user-1"), eq(request))).thenReturn(responseDTO);

        ResponseEntity<ApiResponse<UserResponseDTO>> response = userController.update("user-1", request, file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(responseDTO);
        assertThat(request.getFile()).isEqualTo(file);
        verify(userUseCase).updateUser("user-1", request);
    }

    @Test
    @DisplayName("givenUserId_whenDeleteUser_thenReturnSuccessResponse")
    void givenUserId_whenDeleteUser_thenReturnSuccessResponse() {
        doNothing().when(userUseCase).deleteUser("user-1");

        ResponseEntity<ApiResponse<Void>> response = userController.delete("user-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("SUCCESS");
        verify(userUseCase).deleteUser("user-1");
    }

    @Test
    @DisplayName("givenValidResetPasswordRequest_whenResetPassword_thenReturnUpdatedResponse")
    void givenValidResetPasswordRequest_whenResetPassword_thenReturnUpdatedResponse() {
        UserResetPasswordRequestDTO request = new UserResetPasswordRequestDTO();
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass");

        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setId("user-1");

        when(userUseCase.resetPassword("user-1", request)).thenReturn(responseDTO);

        ResponseEntity<ApiResponse<UserResponseDTO>> response = userController.resetPassword("user-1", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo(responseDTO);
        verify(userUseCase).resetPassword("user-1", request);
    }
}
