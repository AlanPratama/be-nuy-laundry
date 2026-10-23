package com.be_nuy_laundry.be_nuy_laundry.domain.model;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public class BaseModel {

    private String id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
