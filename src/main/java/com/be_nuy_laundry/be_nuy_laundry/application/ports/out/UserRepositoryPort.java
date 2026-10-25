package com.be_nuy_laundry.be_nuy_laundry.application.ports.out;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepositoryPort {
    UserModel save(UserModel userModel);
    Optional<UserModel> findById(String id);
    Optional<UserModel> findByUsername(String username);
    Page<UserModel> findAll(UserFilter userFilter, Pageable pageable);
    void deleteById(String id);
    com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.UserMetricsDTO getMetrics();
}
