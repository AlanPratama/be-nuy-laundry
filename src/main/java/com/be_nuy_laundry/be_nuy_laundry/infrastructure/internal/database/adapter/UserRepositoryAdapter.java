package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.adapter;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.UserRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.UserEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters.UserFilter;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper.UserMapper;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository.UserJpaRepository;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.specifications.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public UserModel save(UserModel user) {
        UserEntity entity = UserMapper.mapToEntity(user);

        UserEntity savedEntity = userJpaRepository.save(entity);

        return UserMapper.mapToDomain(savedEntity);
    }

    @Override
    public Optional<UserModel> findById(String id) {
        return userJpaRepository.findById(id).map(UserMapper::mapToDomain);
    }

    @Override
    public Optional<UserModel> findByUsername(String username) {
        return userJpaRepository.findByUsername(username).map(UserMapper::mapToDomain);
    }

    @Override
    public Page<UserModel> findAll(UserFilter userFilter, Pageable pageable) {
        Specification<UserEntity> specification = UserSpecification.getFilterSpecification(userFilter);

        return userJpaRepository.findAll(specification, pageable).map(UserMapper::mapToDomain);
    }

    @Override
    public void deleteById(String id) {
        userJpaRepository.deleteById(id);
    }
}
