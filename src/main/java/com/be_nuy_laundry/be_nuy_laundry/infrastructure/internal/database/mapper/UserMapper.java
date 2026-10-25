package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper;

import com.be_nuy_laundry.be_nuy_laundry.common.util.ValueUtil;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.UserEntity;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.UserResponseDTO;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.List;

public class UserMapper {

    public static UserEntity mapToEntity(UserModel model) {
        if (model == null)
            return null;

        UserEntity entity = new UserEntity();
        entity.setId(model.getId());
        entity.setName(model.getName());
        entity.setUsername(model.getUsername());
        entity.setPassword(model.getPassword());
        entity.setPhone(model.getPhone());
        entity.setAddress(model.getAddress());
        entity.setRole(model.getRole());
        entity.setProfilePicture(model.getProfilePicture());
        entity.setIsActive(model.getIsActive());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUpdatedAt(model.getUpdatedAt());

        entity.setTransactions(TransactionMapper.mapToEntityList(model.getTransactions()));

        return entity;
    }

    public static List<UserEntity> mapToEntity(List<UserModel> models) {
        if (models == null)
            return new ArrayList<>();

        return models.stream()
                .map(UserMapper::mapToEntity)
                .toList();
    }

    public static UserModel mapToDomain(UserEntity entity) {
        if (entity == null)
            return null;

        UserModel model = new UserModel();
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setUsername(entity.getUsername());
        model.setPassword(entity.getPassword());
        model.setPhone(entity.getPhone());
        model.setAddress(entity.getAddress());
        model.setRole(entity.getRole());
        model.setProfilePicture(entity.getProfilePicture());
        model.setIsActive(entity.getIsActive());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());

        if (Hibernate.isInitialized(entity.getTransactions()) && ValueUtil.hasValueList(entity.getTransactions()))
            model.setTransactions(TransactionMapper.mapToDomainList(entity.getTransactions()));

        return model;
    }

    public static List<UserModel> mapToDomainList(List<UserEntity> entities) {
        if (entities == null)
            return new ArrayList<>();

        return entities.stream()
                .map(UserMapper::mapToDomain)
                .toList();
    }

    public static UserResponseDTO mapEntityToResponse(UserEntity entity) {
        if (entity == null)
            return null;

        UserResponseDTO userResponse = new UserResponseDTO();
        userResponse.setId(entity.getId());
        userResponse.setName(entity.getName());
        userResponse.setUsername(entity.getUsername());
        userResponse.setPhone(entity.getPhone());
        userResponse.setAddress(entity.getAddress());
        userResponse.setRole(entity.getRole());
        userResponse.setProfilePicture(entity.getProfilePicture());
        userResponse.setIsActive(entity.getIsActive());
        userResponse.setCreatedAt(entity.getCreatedAt());
        userResponse.setUpdatedAt(entity.getUpdatedAt());

        if (Hibernate.isInitialized(entity.getTransactions()) && ValueUtil.hasValueList(entity.getTransactions()))
            userResponse.setTransactions(TransactionMapper.mapToDomainList(entity.getTransactions()));

        return userResponse;
    }

    public static List<UserResponseDTO> mapEntityToResponseList(List<UserEntity> entities) {
        if (entities == null)
            return new ArrayList<>();

        return entities.stream()
                .map(UserMapper::mapEntityToResponse)
                .toList();
    }

    public static UserEntity mapResponseToEntity(UserResponseDTO userResponseDTO) {
        if (userResponseDTO == null)
            return null;

        UserEntity entity = new UserEntity();
        entity.setId(userResponseDTO.getId());
        entity.setName(userResponseDTO.getName());
        entity.setUsername(userResponseDTO.getUsername());
        entity.setPhone(userResponseDTO.getPhone());
        entity.setAddress(userResponseDTO.getAddress());
        entity.setRole(userResponseDTO.getRole());
        entity.setProfilePicture(userResponseDTO.getProfilePicture());
        entity.setIsActive(userResponseDTO.getIsActive());
        entity.setCreatedAt(userResponseDTO.getCreatedAt());
        entity.setUpdatedAt(userResponseDTO.getUpdatedAt());

        if (Hibernate.isInitialized(userResponseDTO.getTransactions()) && ValueUtil.hasValueList(userResponseDTO.getTransactions()))
            entity.setTransactions(TransactionMapper.mapToEntityList(userResponseDTO.getTransactions()));

        return entity;
    }

    public static List<UserEntity> mapResponseToEntityList(List<UserResponseDTO> userResponseDTOs) {
        if (userResponseDTOs == null)
            return new ArrayList<>();

        return userResponseDTOs.stream()
                .map(UserMapper::mapResponseToEntity)
                .toList();
    }

    public static UserResponseDTO mapDomainToResponse(UserModel model) {
        if (model == null)
            return null;

        UserResponseDTO userResponse = new UserResponseDTO();
        userResponse.setId(model.getId());
        userResponse.setName(model.getName());
        userResponse.setUsername(model.getUsername());
        userResponse.setPhone(model.getPhone());
        userResponse.setAddress(model.getAddress());
        userResponse.setRole(model.getRole());
        userResponse.setProfilePicture(model.getProfilePicture());
        userResponse.setIsActive(model.getIsActive());
        userResponse.setCreatedAt(model.getCreatedAt());
        userResponse.setUpdatedAt(model.getUpdatedAt());

        if (Hibernate.isInitialized(model.getTransactions()) && ValueUtil.hasValueList(model.getTransactions()))
            userResponse.setTransactions(model.getTransactions());

        return userResponse;
    }

    public static List<UserResponseDTO> mapDomainToResponseList(List<UserModel> models) {
        if (models == null)
            return new ArrayList<>();

        return models.stream()
                .map(UserMapper::mapDomainToResponse)
                .toList();
    }

}
