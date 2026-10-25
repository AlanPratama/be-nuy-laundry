package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.UserUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.response.PageResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.util.PageUtil;
import com.be_nuy_laundry.be_nuy_laundry.common.util.ResponseUtil;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.UserFilter;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.user.UserCreateRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.user.UserResetPasswordRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.user.UserUpdateRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.UserResponseDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.validations.ValidImage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserUseCase userUseCase;

    private final String SUCCESS_RETRIEVE_DATA_MSG = "SUCCESS";
    private final String SUCCESS_CREATE_DATA_MSG = "SUCCESS";
    private final String SUCCESS_UPDATE_DATA_MSG = "SUCCESS";
    private final String SUCCESS_DELETE_DATA_MSG = "SUCCESS";
    private final String SUCCESS_RESET_PASSWORD_MSG = "SUCCESS";

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> create(
            @RequestPart("request") @Valid UserCreateRequestDTO request,
            @RequestPart(value = "file", required = false) @ValidImage(required = false) MultipartFile file
    ) {
        request.setFile(file);

        UserResponseDTO created = userUseCase.createUser(request);
        return ResponseUtil.success(SUCCESS_CREATE_DATA_MSG, created);
    }

    @GetMapping("/metrics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.UserMetricsDTO>> getMetrics() {
        return ResponseUtil.success(SUCCESS_RETRIEVE_DATA_MSG, userUseCase.getMetrics());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getById(@PathVariable String id) {
        validateUserAccess(id);
        return ResponseUtil.success(SUCCESS_RETRIEVE_DATA_MSG, userUseCase.getUserById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponseDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.UserRole role
    ) {
        UserFilter userFilter = UserFilter.builder()
                .name(name)
                .username(username)
                .phone(phone)
                .role(role)
                .build();

        Pageable pageable = PageUtil.setPageable(page, size, sortBy, direction);

        return ResponseUtil.successPage(SUCCESS_RETRIEVE_DATA_MSG, userUseCase.getAllUsers(userFilter, pageable));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponseDTO>> update(
            @PathVariable String id,
            @RequestPart("request") @Valid UserUpdateRequestDTO request,
            @RequestPart(value = "file", required = false) @ValidImage(required = false) MultipartFile file
    ) {
        validateUserAccess(id);
        request.setFile(file);

        return ResponseUtil.success(SUCCESS_UPDATE_DATA_MSG, userUseCase.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        userUseCase.deleteUser(id);
        return ResponseUtil.success(SUCCESS_DELETE_DATA_MSG, null);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponseDTO>> resetPassword(
            @PathVariable String id,
            @Valid @RequestBody UserResetPasswordRequestDTO request
    ) {
        validateUserAccess(id);
        return ResponseUtil.success(SUCCESS_RESET_PASSWORD_MSG, userUseCase.resetPassword(id, request));
    }

    private void validateUserAccess(String userId) {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return;
        }
        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isSuperAdmin) {
            UserResponseDTO existing = userUseCase.getUserById(userId);
            boolean isSelf = existing.getUsername().equals(auth.getName());
            if (!isSelf) {
                if (isAdmin) {
                    if (existing.getRole() != com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.UserRole.STAFF) {
                        throw new com.be_nuy_laundry.be_nuy_laundry.common.exception.ForbiddenException("Admin hanya diperbolehkan mengelola data Staff");
                    }
                } else {
                    throw new com.be_nuy_laundry.be_nuy_laundry.common.exception.ForbiddenException("Staff tidak memiliki izin untuk mengubah data pengguna lain");
                }
            }
        }
    }

}
