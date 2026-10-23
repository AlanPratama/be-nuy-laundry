package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.user;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserCreateRequestDTO {

    @NotBlank(message = "Nama tidak boleh kosong")
    private String name;

    @NotBlank(message = "Username tidak boleh kosong")
    private String username;

    @NotBlank(message = "Password tidak boleh kosong")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$",
            message = "Password harus minimal 8 karakter, mengandung huruf besar, huruf kecil, angka, dan karakter spesial"
    )
    private String password;

    @NotBlank(message = "Nama tidak boleh kosong")
    private String phone;

    private String address;

    private UserRole role;

    @JsonIgnore
    MultipartFile file;

    public UserModel toModel() {
        return UserModel.builder()
                .name(this.getName())
                .username(this.getUsername())
                .password(this.getPassword())
                .phone(this.getPhone())
                .address(this.getAddress())
                .role(this.role)
                .build();
    }

}
