package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.FileUseCase;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.UserRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.ConflictException;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.DataNotFoundException;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.ForbiddenException;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserModel;
import com.be_nuy_laundry.be_nuy_laundry.fixture.TestDataBuilder;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.UserRole;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.UserFilter;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.user.UserCreateRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.user.UserResetPasswordRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.user.UserUpdateRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.UserResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FileUseCase fileUseCase;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("givenUserCreateRequestWithFile_whenCreateUser_thenUploadFileAndSaveUser")
    void givenUserCreateRequestWithFile_whenCreateUser_thenUploadFileAndSaveUser() {
        UserCreateRequestDTO request = new UserCreateRequestDTO();
        request.setName("John Staff");
        request.setUsername("johnstaff");
        request.setPassword("Secret@123");
        request.setPhone("0812345678");
        request.setAddress("Jl. Sudirman");
        request.setRole(UserRole.STAFF);
        request.setFile(new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "imageContent".getBytes()));

        when(userRepositoryPort.findByUsername("johnstaff")).thenReturn(Optional.empty());
        when(fileUseCase.upload(any(), eq("johnstaff"), anyString())).thenReturn("/users/avatar.jpg");
        when(passwordEncoder.encode("Secret@123")).thenReturn("encodedPassword");
        when(userRepositoryPort.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO response = userService.createUser(request);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("johnstaff");
        assertThat(response.getProfilePicture()).isEqualTo("/users/avatar.jpg");
        assertThat(response.getRole()).isEqualTo(UserRole.STAFF);
    }

    @Test
    @DisplayName("givenUserCreateRequestWithoutFileAndNullRole_whenCreateUser_thenUseDefaultValues")
    void givenUserCreateRequestWithoutFileAndNullRole_whenCreateUser_thenUseDefaultValues() {
        UserCreateRequestDTO request = new UserCreateRequestDTO();
        request.setName("Jane Admin");
        request.setUsername("janeadmin");
        request.setPassword("Secret@123");
        request.setPhone("0812345678");
        request.setRole(null);
        request.setFile(null);

        when(userRepositoryPort.findByUsername("janeadmin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Secret@123")).thenReturn("encodedPassword");
        when(userRepositoryPort.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO response = userService.createUser(request);

        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo(UserRole.STAFF);
        verifyNoInteractions(fileUseCase);
    }

    @Test
    @DisplayName("givenExistingUsername_whenCreateUser_thenThrowConflictException")
    void givenExistingUsername_whenCreateUser_thenThrowConflictException() {
        UserCreateRequestDTO request = new UserCreateRequestDTO();
        request.setUsername("existinguser");

        when(userRepositoryPort.findByUsername("existinguser")).thenReturn(Optional.of(TestDataBuilder.createSampleUser()));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("User sudah tersedia");
    }

    @Test
    @DisplayName("givenExistingId_whenGetDomainUserById_thenReturnUserModel")
    void givenExistingId_whenGetDomainUserById_thenReturnUserModel() {
        UserModel user = TestDataBuilder.createSampleUser();
        when(userRepositoryPort.findById(user.getId())).thenReturn(Optional.of(user));

        UserModel result = userService.getDomainUserById(user.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("givenMissingId_whenGetDomainUserById_thenThrowDataNotFoundException")
    void givenMissingId_whenGetDomainUserById_thenThrowDataNotFoundException() {
        when(userRepositoryPort.findById("missing-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getDomainUserById("missing-id"))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("User tidak ditemukan");
    }

    @Test
    @DisplayName("givenExistingId_whenGetUserById_thenReturnUserResponseDTO")
    void givenExistingId_whenGetUserById_thenReturnUserResponseDTO() {
        UserModel user = TestDataBuilder.createSampleUser();
        when(userRepositoryPort.findById(user.getId())).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.getUserById(user.getId());

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("givenMissingId_whenGetUserById_thenThrowDataNotFoundException")
    void givenMissingId_whenGetUserById_thenThrowDataNotFoundException() {
        when(userRepositoryPort.findById("missing-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById("missing-id"))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("User tidak ditemukan");
    }

    @Test
    @DisplayName("givenExistingUsername_whenGetUserByUsername_thenReturnUserResponseDTO")
    void givenExistingUsername_whenGetUserByUsername_thenReturnUserResponseDTO() {
        UserModel user = TestDataBuilder.createSampleUser();
        when(userRepositoryPort.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.getUserByUsername("testuser");

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("givenMissingUsername_whenGetUserByUsername_thenThrowDataNotFoundException")
    void givenMissingUsername_whenGetUserByUsername_thenThrowDataNotFoundException() {
        when(userRepositoryPort.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByUsername("unknown"))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("User tidak ditemukan");
    }

    @Test
    @DisplayName("givenExistingUsername_whenGetDomainUserByUsername_thenReturnUserModel")
    void givenExistingUsername_whenGetDomainUserByUsername_thenReturnUserModel() {
        UserModel user = TestDataBuilder.createSampleUser();
        when(userRepositoryPort.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserModel result = userService.getDomainUserByUsername("testuser");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("givenMissingUsername_whenGetDomainUserByUsername_thenThrowDataNotFoundException")
    void givenMissingUsername_whenGetDomainUserByUsername_thenThrowDataNotFoundException() {
        when(userRepositoryPort.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getDomainUserByUsername("unknown"))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("User tidak ditemukan");
    }

    @Test
    @DisplayName("givenFilterAndPageable_whenGetAllUsers_thenReturnPagedUserResponseDTO")
    void givenFilterAndPageable_whenGetAllUsers_thenReturnPagedUserResponseDTO() {
        UserModel user = TestDataBuilder.createSampleUser();
        Page<UserModel> userPage = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);
        UserFilter filter = UserFilter.builder().build();

        when(userRepositoryPort.findAll(filter, PageRequest.of(0, 10))).thenReturn(userPage);

        Page<UserResponseDTO> result = userService.getAllUsers(filter, PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("givenUpdateRequestWithNewAvatar_whenUpdateUser_thenDeleteOldAvatarAndUploadNew")
    void givenUpdateRequestWithNewAvatar_whenUpdateUser_thenDeleteOldAvatarAndUploadNew() {
        UserModel existingUser = TestDataBuilder.createSampleUser();
        existingUser.setProfilePicture("/old/avatar.png");

        UserUpdateRequestDTO request = new UserUpdateRequestDTO();
        request.setName("Updated Name");
        request.setUsername("updateduser");
        request.setPhone("089999999");
        request.setAddress("Updated Address");
        request.setRole(UserRole.ADMIN);
        request.setIsActive(true);
        request.setFile(new MockMultipartFile("file", "new-avatar.png", "image/png", "newContent".getBytes()));

        when(userRepositoryPort.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(fileUseCase.upload(any(), eq("updateduser"), anyString())).thenReturn("/new/avatar.png");
        when(userRepositoryPort.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO response = userService.updateUser(existingUser.getId(), request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Updated Name");
        assertThat(response.getProfilePicture()).isEqualTo("/new/avatar.png");
        verify(fileUseCase).delete("/old/avatar.png");
    }

    @Test
    @DisplayName("givenMissingUser_whenUpdateUser_thenThrowDataNotFoundException")
    void givenMissingUser_whenUpdateUser_thenThrowDataNotFoundException() {
        when(userRepositoryPort.findById("missing-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser("missing-id", new UserUpdateRequestDTO()))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("User tidak ditemukan");
    }

    @Test
    @DisplayName("givenValidId_whenDeleteUser_thenInvokeRepositoryDelete")
    void givenValidId_whenDeleteUser_thenInvokeRepositoryDelete() {
        UserModel existingUser = TestDataBuilder.createSampleUser();
        when(userRepositoryPort.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));

        userService.deleteUser(existingUser.getId());

        verify(userRepositoryPort).deleteById(existingUser.getId());
    }

    @Test
    @DisplayName("givenValidPasswordReset_whenResetPassword_thenEncodeAndSaveNewPassword")
    void givenValidPasswordReset_whenResetPassword_thenEncodeAndSaveNewPassword() {
        UserModel user = TestDataBuilder.createSampleUser();
        user.setPassword("oldHashedPass");

        UserResetPasswordRequestDTO request = new UserResetPasswordRequestDTO();
        request.setOldPassword("oldPlainPass");
        request.setNewPassword("NewSecret@123");

        when(userRepositoryPort.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPlainPass", "oldHashedPass")).thenReturn(true);
        when(passwordEncoder.encode("NewSecret@123")).thenReturn("newHashedPass");
        when(userRepositoryPort.save(any(UserModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO response = userService.resetPassword(user.getId(), request);

        assertThat(response).isNotNull();
        verify(userRepositoryPort).save(user);
    }

    @Test
    @DisplayName("givenInvalidOldPassword_whenResetPassword_thenThrowForbiddenException")
    void givenInvalidOldPassword_whenResetPassword_thenThrowForbiddenException() {
        UserModel user = TestDataBuilder.createSampleUser();
        user.setPassword("oldHashedPass");

        UserResetPasswordRequestDTO request = new UserResetPasswordRequestDTO();
        request.setOldPassword("wrongOldPass");
        request.setNewPassword("NewSecret@123");

        when(userRepositoryPort.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOldPass", "oldHashedPass")).thenReturn(false);

        assertThatThrownBy(() -> userService.resetPassword(user.getId(), request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Password tidak valid");
    }
}
