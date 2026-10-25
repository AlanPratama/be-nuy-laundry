package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.user;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserUpdateRequestDTO {

    @NotBlank(message = "Nama tidak boleh kosong")
    private String name;

    @NotBlank(message = "Username tidak boleh kosong")
    private String username;

    @NotBlank(message = "Nama tidak boleh kosong")
    private String phone;

    private String address;

    private UserRole role;

    private Boolean isActive;

    private Boolean removeProfilePicture;

    @JsonIgnore
    MultipartFile file;

}
