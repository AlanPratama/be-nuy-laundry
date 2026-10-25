package com.be_nuy_laundry.be_nuy_laundry.application.ports.out;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.ArticleModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ArticleRepositoryPort {

    ArticleModel save(ArticleModel model);

    Optional<ArticleModel> findById(String id);

    Optional<ArticleModel> findBySlug(String slug);

    Page<ArticleModel> findAll(String search, String category, Boolean isPublished, Pageable pageable);

    void deleteById(String id);

    boolean existsBySlug(String slug);

    boolean existsByTitle(String title);
}
