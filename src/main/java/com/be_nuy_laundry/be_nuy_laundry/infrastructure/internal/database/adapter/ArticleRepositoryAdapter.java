package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.adapter;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.ArticleRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.ArticleModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.ArticleEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper.ArticleMapper;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository.ArticleJpaRepository;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.specifications.ArticleSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ArticleRepositoryAdapter implements ArticleRepositoryPort {

    private final ArticleJpaRepository articleJpaRepository;

    @Override
    public ArticleModel save(ArticleModel model) {
        ArticleEntity entity = ArticleMapper.mapToEntity(model);
        ArticleEntity saved = articleJpaRepository.save(entity);
        return ArticleMapper.mapToDomain(saved);
    }

    @Override
    public Optional<ArticleModel> findById(String id) {
        return articleJpaRepository.findById(id).map(ArticleMapper::mapToDomain);
    }

    @Override
    public Optional<ArticleModel> findBySlug(String slug) {
        return articleJpaRepository.findBySlug(slug).map(ArticleMapper::mapToDomain);
    }

    @Override
    public Page<ArticleModel> findAll(String search, String category, Boolean isPublished, Pageable pageable) {
        Specification<ArticleEntity> spec = ArticleSpecification.getFilterSpecification(search, category, isPublished);
        return articleJpaRepository.findAll(spec, pageable).map(ArticleMapper::mapToDomain);
    }

    @Override
    public void deleteById(String id) {
        articleJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return articleJpaRepository.existsBySlug(slug);
    }

    @Override
    public boolean existsByTitle(String title) {
        return articleJpaRepository.existsByTitle(title);
    }
}
