package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.ArticleModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.ArticleEntity;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.article.ArticleResponseDTO;

public class ArticleMapper {

    public static ArticleEntity mapToEntity(ArticleModel model) {
        if (model == null) return null;

        ArticleEntity entity = new ArticleEntity();
        entity.setId(model.getId());
        entity.setTitle(model.getTitle());
        entity.setSlug(model.getSlug());
        entity.setCategory(model.getCategory());
        entity.setExcerpt(model.getExcerpt());
        entity.setContent(model.getContent());
        entity.setReadingTime(model.getReadingTime());
        entity.setThumbnailUrl(model.getThumbnailUrl());
        entity.setIsPublished(Boolean.TRUE.equals(model.getIsPublished()));
        entity.setAuthorId(model.getAuthorId());
        entity.setPublishedAt(model.getPublishedAt());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUpdatedAt(model.getUpdatedAt());

        return entity;
    }

    public static ArticleModel mapToDomain(ArticleEntity entity) {
        if (entity == null) return null;

        String authorName = null;
        if (entity.getAuthor() != null) {
            authorName = entity.getAuthor().getName() != null ? entity.getAuthor().getName() : entity.getAuthor().getUsername();
        }

        ArticleModel model = new ArticleModel();
        model.setId(entity.getId());
        model.setTitle(entity.getTitle());
        model.setSlug(entity.getSlug());
        model.setCategory(entity.getCategory());
        model.setExcerpt(entity.getExcerpt());
        model.setContent(entity.getContent());
        model.setReadingTime(entity.getReadingTime());
        model.setThumbnailUrl(entity.getThumbnailUrl());
        model.setIsPublished(Boolean.TRUE.equals(entity.getIsPublished()));
        model.setAuthorId(entity.getAuthorId());
        model.setAuthorName(authorName);
        model.setPublishedAt(entity.getPublishedAt());
        model.setCreatedAt(entity.getCreatedAt());
        model.setUpdatedAt(entity.getUpdatedAt());

        return model;
    }

    public static ArticleResponseDTO mapDomainToResponse(ArticleModel model) {
        if (model == null) return null;

        return ArticleResponseDTO.builder()
                .id(model.getId())
                .title(model.getTitle())
                .slug(model.getSlug())
                .category(model.getCategory())
                .excerpt(model.getExcerpt())
                .content(model.getContent())
                .readingTime(model.getReadingTime())
                .thumbnailUrl(model.getThumbnailUrl())
                .isPublished(model.getIsPublished())
                .authorId(model.getAuthorId())
                .authorName(model.getAuthorName())
                .publishedAt(model.getPublishedAt())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }

    public static ArticleResponseDTO mapEntityToResponse(ArticleEntity entity) {
        if (entity == null) return null;
        return mapDomainToResponse(mapToDomain(entity));
    }
}
