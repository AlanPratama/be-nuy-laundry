package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.UserRole;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.STAFF;

    private String profilePicture;

    private Boolean isActive;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<TransactionEntity> transactions;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<UserSessionEntity> userSessions;

}
