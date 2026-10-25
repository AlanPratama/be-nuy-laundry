package com.be_nuy_laundry.be_nuy_laundry.application.ports.in;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.UserFilter;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.user.UserCreateRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.user.UserResetPasswordRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.user.UserUpdateRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserUseCase {
    UserResponseDTO createUser(UserCreateRequestDTO requestDTO);
    UserModel getDomainUserById(String id);
    UserResponseDTO getUserById(String id);
    UserResponseDTO getUserByUsername(String username);
    UserModel getDomainUserByUsername(String username);
    Page<UserResponseDTO> getAllUsers(UserFilter userFilter, Pageable pageable);
    UserResponseDTO updateUser(String id, UserUpdateRequestDTO requestDTO);
    void deleteUser(String id);
    UserResponseDTO resetPassword(String id, UserResetPasswordRequestDTO requestDTO);
    com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.UserMetricsDTO getMetrics();
}