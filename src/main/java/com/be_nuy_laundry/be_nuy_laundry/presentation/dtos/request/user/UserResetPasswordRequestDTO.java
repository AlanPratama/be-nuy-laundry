package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserResetPasswordRequestDTO {

    @NotBlank(message = "Password tidak boleh kosong")
    private String oldPassword;

    @NotBlank(message = "Password baru tidak boleh kosong")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$",
            message = "Password harus minimal 8 karakter, mengandung huruf besar, huruf kecil, angka, dan karakter spesial"
    )
    private String newPassword;

}
