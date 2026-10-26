package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.ArticleUseCase;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.FileUseCase;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.ArticleRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.UserRepositoryPort;
import com.be_nuy_laundry.be_nuy_laundry.common.constant.MinIoConstant;
import com.be_nuy_laundry.be_nuy_laundry.common.exception.DataNotFoundException;
import com.be_nuy_laundry.be_nuy_laundry.common.util.ValueUtil;
import com.be_nuy_laundry.be_nuy_laundry.domain.model.ArticleModel;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.mapper.ArticleMapper;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.article.ArticleRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.article.ArticleResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleServiceImpl implements ArticleUseCase {

    private final ArticleRepositoryPort articleRepositoryPort;
    private final FileUseCase fileUseCase;
    private final UserRepositoryPort userRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleResponseDTO> getAllArticles(String search, String category, Boolean isPublished, Pageable pageable) {
        return articleRepositoryPort.findAll(search, category, isPublished, pageable)
                .map(ArticleMapper::mapDomainToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleResponseDTO getArticleByIdOrSlug(String idOrSlug) {
        ArticleModel model = articleRepositoryPort.findBySlug(idOrSlug)
                .or(() -> articleRepositoryPort.findById(idOrSlug))
                .orElseThrow(() -> new DataNotFoundException("Artikel tidak ditemukan"));
        return ArticleMapper.mapDomainToResponse(model);
    }

    @Override
    @Transactional
    public ArticleResponseDTO createArticle(ArticleRequestDTO request, String currentUsername) {
        ArticleModel model = request.toModel();

        // 1. Generate unique slug from title
        String slug = generateSlug(model.getTitle(), null);
        model.setSlug(slug);

        // 2. Reading time calculation if empty
        if (!ValueUtil.hasValueString(model.getReadingTime())) {
            model.setReadingTime(calculateReadingTime(model.getContent()));
        }

        // 3. Resolve Author
        if (ValueUtil.hasValueString(currentUsername)) {
            userRepositoryPort.findByUsername(currentUsername).ifPresent(user -> {
                model.setAuthorId(user.getId());
                model.setAuthorName(user.getName() != null ? user.getName() : user.getUsername());
            });
        }

        // 4. Upload thumbnail if file provided
        String thumbnailUrl = null;
        if (ValueUtil.hasValueMultipartFile(request.getFile())) {
            thumbnailUrl = fileUseCase.upload(
                    request.getFile(),
                    slug,
                    MinIoConstant.FOLDER.ARTICLE
            );
        }
        model.setThumbnailUrl(thumbnailUrl);

        // 5. Published timestamp
        if (Boolean.TRUE.equals(model.getIsPublished())) {
            model.setPublishedAt(LocalDateTime.now());
        }

        ArticleModel saved = articleRepositoryPort.save(model);
        return ArticleMapper.mapDomainToResponse(saved);
    }

    @Override
    @Transactional
    public ArticleResponseDTO updateArticle(String id, ArticleRequestDTO request, String currentUsername) {
        ArticleModel existing = articleRepositoryPort.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Artikel tidak ditemukan"));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            if (!request.getTitle().equalsIgnoreCase(existing.getTitle())) {
                existing.setTitle(request.getTitle().trim());
                existing.setSlug(generateSlug(existing.getTitle(), existing.getId()));
            }
        }

        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            existing.setCategory(request.getCategory().trim());
        }

        if (request.getExcerpt() != null) {
            existing.setExcerpt(request.getExcerpt().trim());
        }

        if (request.getContent() != null && !request.getContent().isBlank()) {
            existing.setContent(request.getContent().trim());
        }

        if (request.getReadingTime() != null && !request.getReadingTime().isBlank()) {
            existing.setReadingTime(request.getReadingTime().trim());
        } else if (request.getContent() != null && !request.getContent().isBlank()) {
            existing.setReadingTime(calculateReadingTime(existing.getContent()));
        }

        // Thumbnail updates
        if (ValueUtil.hasValueMultipartFile(request.getFile())) {
            if (ValueUtil.hasValueString(existing.getThumbnailUrl())) {
                try {
                    fileUseCase.delete(existing.getThumbnailUrl());
                } catch (Exception e) {
                    log.warn("Failed to delete old thumbnail: {}", e.getMessage());
                }
            }
            String newThumb = fileUseCase.upload(request.getFile(), existing.getSlug(), MinIoConstant.FOLDER.ARTICLE);
            existing.setThumbnailUrl(newThumb);
        } else if (Boolean.TRUE.equals(request.getRemoveThumbnail())) {
            if (ValueUtil.hasValueString(existing.getThumbnailUrl())) {
                try {
                    fileUseCase.delete(existing.getThumbnailUrl());
                } catch (Exception e) {
                    log.warn("Failed to delete thumbnail: {}", e.getMessage());
                }
            }
            existing.setThumbnailUrl(null);
        }

        if (request.getIsPublished() != null) {
            boolean nextPublished = Boolean.TRUE.equals(request.getIsPublished());
            if (nextPublished && !Boolean.TRUE.equals(existing.getIsPublished()) && existing.getPublishedAt() == null) {
                existing.setPublishedAt(LocalDateTime.now());
            }
            existing.setIsPublished(nextPublished);
        }

        ArticleModel updated = articleRepositoryPort.save(existing);
        return ArticleMapper.mapDomainToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteArticle(String id) {
        ArticleModel existing = articleRepositoryPort.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Artikel tidak ditemukan"));

        if (ValueUtil.hasValueString(existing.getThumbnailUrl())) {
            try {
                fileUseCase.delete(existing.getThumbnailUrl());
            } catch (Exception e) {
                log.warn("Failed to delete thumbnail on article delete: {}", e.getMessage());
            }
        }

        articleRepositoryPort.deleteById(id);
    }

    @Override
    @Transactional
    public ArticleResponseDTO togglePublish(String id) {
        ArticleModel existing = articleRepositoryPort.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Artikel tidak ditemukan"));

        boolean nextPublished = !Boolean.TRUE.equals(existing.getIsPublished());
        existing.setIsPublished(nextPublished);
        if (nextPublished && existing.getPublishedAt() == null) {
            existing.setPublishedAt(LocalDateTime.now());
        }

        ArticleModel saved = articleRepositoryPort.save(existing);
        return ArticleMapper.mapDomainToResponse(saved);
    }

    private String generateSlug(String title, String currentArticleId) {
        if (title == null || title.isBlank()) {
            return "artikel-" + UUID.randomUUID().toString().substring(0, 8);
        }

        String baseSlug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");

        if (baseSlug.isEmpty()) {
            baseSlug = "artikel-" + UUID.randomUUID().toString().substring(0, 8);
        }

        String slug = baseSlug;
        int counter = 1;
        while (true) {
            Optional<ArticleModel> existing = articleRepositoryPort.findBySlug(slug);
            if (existing.isEmpty() || (currentArticleId != null && existing.get().getId().equals(currentArticleId))) {
                break;
            }
            counter++;
            slug = baseSlug + "-" + counter;
        }
        return slug;
    }

    private String calculateReadingTime(String content) {
        if (content == null || content.isBlank()) {
            return "1 menit baca";
        }
        String text = content.replaceAll("<[^>]*>", " ");
        String[] words = text.trim().split("\\s+");
        int wordCount = words.length;
        int minutes = Math.max(1, (int) Math.ceil((double) wordCount / 180.0));
        return minutes + " menit baca";
    }

    @Override
    public String uploadArticleImage(org.springframework.web.multipart.MultipartFile file) {
        String fileName = UUID.randomUUID().toString();
        return fileUseCase.upload(file, fileName, MinIoConstant.FOLDER.ARTICLE);
    }

    @Override
    @Transactional(readOnly = true)
    public com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.article.ArticleMetricsDTO getMetrics() {
        long published = articleRepositoryPort.countByIsPublished(true);
        long drafts = articleRepositoryPort.countByIsPublished(false);
        long total = articleRepositoryPort.count();
        return com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.article.ArticleMetricsDTO.builder()
                .totalArticles(total)
                .publishedArticles(published)
                .draftArticles(drafts)
                .build();
    }
}
