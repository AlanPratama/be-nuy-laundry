package com.be_nuy_laundry.be_nuy_laundry.domain.model;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.UserRole;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserModel extends BaseModel {
    private String name;
    private String username;
    private String password;
    private String phone;
    private String address;
    private UserRole role;
    private String profilePicture;
    private Boolean isActive;
    private List<TransactionModel> transactions;
}
