package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.UserSessionModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.UserSessionEntity;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.auth.AuthResponseDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.auth.UserSessionResponseDTO;

import java.util.ArrayList;
import java.util.List;

public class UserSessionMapper {

    public static UserSessionEntity mapToEntity(UserSessionModel model) {
        if (model == null)
            return null;

        UserSessionEntity entity = new UserSessionEntity();
        entity.setId(model.getId());
        entity.setAccessToken(model.getAccessToken());
        entity.setRefreshToken(model.getRefreshToken());
        entity.setIsActive(model.getIsActive());
        entity.setDeviceInfo(model.getDeviceInfo());
        entity.setExpiresAt(model.getExpiresAt());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUpdatedAt(model.getUpdatedAt());

        entity.setUser(UserMapper.mapToEntity(model.getUser()));

        return entity;
    }

    public static List<UserSessionEntity> mapToEntityList(List<UserSessionModel> models) {
        if (models == null)
            return new ArrayList<>();

        return models.stream()
                .map(UserSessionMapper::mapToEntity)
                .toList();
    }

    public static UserSessionModel mapToDomain(UserSessionEntity entity) {
        if (entity == null)
            return null;

        UserSessionModel model = new UserSessionModel();
        model.setId(entity.getId());
        model.setAccessToken(entity.getAccessToken());
        model.setRefreshToken(entity.getRefreshToken());
        model.setIsActive(entity.getIsActive());
        model.setDeviceInfo(entity.getDeviceInfo());
        model.setExpiresAt(entity.getExpiresAt());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());

        model.setUser(UserMapper.mapToDomain(entity.getUser()));

        return model;
    }

    public static List<UserSessionModel> mapToDomainList(List<UserSessionEntity> entities) {
        if (entities == null)
            return new ArrayList<>();

        return entities.stream()
                .map(UserSessionMapper::mapToDomain)
                .toList();
    }

    public static UserSessionResponseDTO mapDomainToResponse(UserSessionModel model) {
        if (model == null)
            return null;

        UserSessionResponseDTO response = new UserSessionResponseDTO();
        response.setId(model.getId());
        response.setAccessToken(model.getAccessToken());
        response.setRefreshToken(model.getRefreshToken());
        response.setIsActive(model.getIsActive());
        response.setDeviceInfo(model.getDeviceInfo());
        response.setExpiresAt(model.getExpiresAt());
        response.setCreatedAt(model.getCreatedAt());
        response.setUpdatedAt(model.getUpdatedAt());

        response.setUser(UserMapper.mapDomainToResponse(model.getUser()));

        return response;
    }

    public static List<UserSessionResponseDTO> mapDomainToResponseList(List<UserSessionModel> models) {
        if (models == null)
            return new ArrayList<>();

        return models.stream()
                .map(UserSessionMapper::mapDomainToResponse)
                .toList();
    }

    public static UserSessionResponseDTO mapEntityToResponse(UserSessionEntity entity) {
        if (entity == null)
            return null;

        UserSessionResponseDTO response = new UserSessionResponseDTO();
        response.setId(entity.getId());
        response.setAccessToken(entity.getAccessToken());
        response.setRefreshToken(entity.getRefreshToken());
        response.setIsActive(entity.getIsActive());
        response.setDeviceInfo(entity.getDeviceInfo());
        response.setExpiresAt(entity.getExpiresAt());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());

        response.setUser(UserMapper.mapEntityToResponse(entity.getUser()));

        return response;
    }

    public static List<UserSessionResponseDTO> mapEntityToResponseList(List<UserSessionEntity> entities) {
        if (entities == null)
            return new ArrayList<>();

        return entities.stream()
                .map(UserSessionMapper::mapEntityToResponse)
                .toList();
    }


    public static AuthResponseDTO mapDomainToAuthResponse(UserSessionModel model) {
        if (model == null)
            return null;

        AuthResponseDTO response = new AuthResponseDTO();
        response.setAccessToken(model.getAccessToken());
        response.setRefreshToken(model.getRefreshToken());
        response.setDeviceInfo(model.getDeviceInfo());

        return response;
    }

    public static List<AuthResponseDTO> mapDomainToAuthResponseList(List<UserSessionModel> models) {
        if (models == null)
            return new ArrayList<>();

        return models.stream()
                .map(UserSessionMapper::mapDomainToAuthResponse)
                .toList();
    }

}
