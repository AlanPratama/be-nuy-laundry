package com.be_nuy_laundry.be_nuy_laundry.domain.enums;

import lombok.Getter;

@Getter
public enum UserErrorMessage {

    USER_NOT_FOUND("User tidak ditemukan di dalam sistem"),
    USER_ALREADY_EXISTS("Username atau Email sudah terdaftar"),
    PASSWORD_INVALID("Password yang Anda masukkan salah"),
    USER_INACTIVE("Akun user ini sedang dinonaktifkan");

    private final String message;

    UserErrorMessage(String message) {
        this.message = message;
    }
}