package com.be_nuy_laundry.be_nuy_laundry.application.ports.in;

import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.article.ArticleRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.article.ArticleResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArticleUseCase {

    Page<ArticleResponseDTO> getAllArticles(String search, String category, Boolean isPublished, Pageable pageable);

    ArticleResponseDTO getArticleByIdOrSlug(String idOrSlug);

    ArticleResponseDTO createArticle(ArticleRequestDTO request, String currentUsername);

    ArticleResponseDTO updateArticle(String id, ArticleRequestDTO request, String currentUsername);

    void deleteArticle(String id);

    ArticleResponseDTO togglePublish(String id);

    String uploadArticleImage(org.springframework.web.multipart.MultipartFile file);
}
