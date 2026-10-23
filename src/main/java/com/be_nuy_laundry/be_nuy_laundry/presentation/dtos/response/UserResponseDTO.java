package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.BaseModel;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.TransactionModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.UserRole;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDTO extends BaseModel {

    private String name;
    private String username;
    private String phone;
    private String address;
    private UserRole role;
    private String profilePicture;
    private Boolean isActive;
    private List<TransactionModel> transactions;

}
