package com.be_nuy_laundry.be_nuy_laundry.domain.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerModel extends BaseModel {
    private String name;
    private String phone;
    private String address;
    private List<TransactionModel> transactions;
}
