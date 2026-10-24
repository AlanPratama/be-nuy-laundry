package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.FileUseCase;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.UserUseCase;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.UserRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.common.constant.Constant;
import com.be_nuy_laundry.be_nuy_laundry.common.constant.MinIoConstant;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.ConflictException;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.DataNotFoundException;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.ForbiddenException;
import com.be_nuy_laundry.be_nuy_laundry.common.util.ValueUtil;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.UserRole;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.UserFilter;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper.UserMapper;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.user.UserCreateRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.user.UserResetPasswordRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.user.UserUpdateRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    private final PasswordEncoder passwordEncoder;

    private final FileUseCase fileUseCase;

    @Override
    public UserResponseDTO createUser(UserCreateRequestDTO requestDTO) {
        String filePath = Constant.EMPTY_STRING;
        Optional<UserModel> existingEntity = userRepositoryPort.findByUsername(requestDTO.getUsername());

        if (existingEntity.isPresent())
            throw new ConflictException("User sudah tersedia");

        if (ValueUtil.hasValueMultipartFile(requestDTO.getFile())) {
            filePath = fileUseCase.upload(
                    requestDTO.getFile(),
                    requestDTO.getUsername(),
                    MinIoConstant.FOLDER.USER);
        }

        UserModel userModel = requestDTO.toModel();
        userModel.setProfilePicture(filePath);
        userModel.setRole(ValueUtil.orElseUserRole(requestDTO.getRole(), UserRole.STAFF));
        userModel.setPassword(passwordEncoder.encode(userModel.getPassword()));
        userModel.setIsActive(Boolean.TRUE);

        return UserMapper.mapDomainToResponse(userRepositoryPort.save(userModel));
    }

    @Override
    public UserModel getDomainUserById(String id) {
        return userRepositoryPort.findById(id)
                .orElseThrow(() -> new DataNotFoundException("User tidak ditemukan"));
    }

    @Override
    public UserResponseDTO getUserById(String id) {
        return UserMapper.mapDomainToResponse(userRepositoryPort.findById(id)
                .orElseThrow(() -> new DataNotFoundException("User tidak ditemukan")));
    }

    @Override
    public UserResponseDTO getUserByUsername(String id) {
        return UserMapper.mapDomainToResponse(userRepositoryPort.findByUsername(id)
                .orElseThrow(() -> new DataNotFoundException("User tidak ditemukan")));
    }

    @Override
    public UserModel getDomainUserByUsername(String id) {
        return userRepositoryPort.findByUsername(id)
                .orElseThrow(() -> new DataNotFoundException("User tidak ditemukan"));
    }

    @Override
    public Page<UserResponseDTO> getAllUsers(UserFilter userFilter, Pageable pageable) {
        return userRepositoryPort.findAll(userFilter, pageable).map(UserMapper::mapDomainToResponse);
    }

    @Override
    public UserResponseDTO updateUser(String id, UserUpdateRequestDTO requestDTO) {
        UserModel existingModel = userRepositoryPort.findById(id)
                .orElseThrow(() -> new DataNotFoundException("User tidak ditemukan"));

        existingModel.setName(requestDTO.getName());
        existingModel.setUsername(requestDTO.getUsername());
        existingModel.setPhone(requestDTO.getPhone());
        existingModel.setAddress(requestDTO.getAddress());
        existingModel.setRole(ValueUtil.orElseUserRole(requestDTO.getRole(), UserRole.STAFF));

        String filePath = existingModel.getProfilePicture();

        if (ValueUtil.hasValueMultipartFile(requestDTO.getFile())) {
            if (ValueUtil.hasValueString(existingModel.getProfilePicture()))
                fileUseCase.delete(existingModel.getProfilePicture());

            filePath = fileUseCase.upload(
                    requestDTO.getFile(),
                    requestDTO.getUsername(),
                    MinIoConstant.FOLDER.USER);
        }

        existingModel.setProfilePicture(filePath);

        return UserMapper.mapDomainToResponse(userRepositoryPort.save(existingModel));
    }

    @Override
    public void deleteUser(String id) {
        this.getUserById(id);
        userRepositoryPort.deleteById(id);
    }

    @Override
    public UserResponseDTO resetPassword(String id, UserResetPasswordRequestDTO requestDTO) {
        UserModel existingUserModel = this.getDomainUserById(id);

        String oldPassword = requestDTO.getOldPassword();
        String newPassword = requestDTO.getNewPassword();

        if (!passwordEncoder.matches(oldPassword, existingUserModel.getPassword()))
            throw new ForbiddenException("Password tidak valid");

        existingUserModel.setPassword(passwordEncoder.encode(newPassword));

        return UserMapper.mapDomainToResponse(userRepositoryPort.save(existingUserModel));
    }

}
