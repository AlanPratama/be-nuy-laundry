package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.ArticleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ArticleJpaRepository extends JpaRepository<ArticleEntity, String>, JpaSpecificationExecutor<ArticleEntity> {

    @EntityGraph(attributePaths = {"author"})
    Optional<ArticleEntity> findBySlug(String slug);

    @EntityGraph(attributePaths = {"author"})
    Optional<ArticleEntity> findById(String id);

    @Override
    @EntityGraph(attributePaths = {"author"})
    Page<ArticleEntity> findAll(Specification<ArticleEntity> spec, Pageable pageable);

    boolean existsBySlug(String slug);

    boolean existsByTitle(String title);
}
