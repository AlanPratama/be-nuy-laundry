package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.ArticleUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.response.PageResponse;
import com.be_nuy_laundry.be_nuy_laundry.common.util.PageUtil;
import com.be_nuy_laundry.be_nuy_laundry.common.util.ResponseUtil;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.article.ArticleRequestDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.article.ArticleResponseDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.validations.ValidImage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
@Validated
public class ArticleController {

    private final ArticleUseCase articleUseCase;

    private final String SUCCESS_RETRIEVE_DATA_MSG = "Berhasil memuat data artikel";
    private final String SUCCESS_CREATE_DATA_MSG = "Artikel berhasil dibuat";
    private final String SUCCESS_UPDATE_DATA_MSG = "Artikel berhasil diperbarui";
    private final String SUCCESS_DELETE_DATA_MSG = "Artikel berhasil dihapus";

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ArticleResponseDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isPublished
    ) {
        Pageable pageable = PageUtil.setPageable(page, size, sortBy, direction);
        return ResponseUtil.successPage(SUCCESS_RETRIEVE_DATA_MSG, articleUseCase.getAllArticles(search, category, isPublished, pageable));
    }

    @GetMapping("/metrics")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.article.ArticleMetricsDTO>> getMetrics() {
        return ResponseUtil.success(SUCCESS_RETRIEVE_DATA_MSG, articleUseCase.getMetrics());
    }

    @GetMapping("/{idOrSlug}")
    public ResponseEntity<ApiResponse<ArticleResponseDTO>> getByIdOrSlug(@PathVariable String idOrSlug) {
        return ResponseUtil.success(SUCCESS_RETRIEVE_DATA_MSG, articleUseCase.getArticleByIdOrSlug(idOrSlug));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ArticleResponseDTO>> create(
            @RequestPart("request") @Valid ArticleRequestDTO request,
            @RequestPart(value = "file", required = false) @ValidImage(required = false) MultipartFile file,
            Authentication authentication
    ) {
        request.setFile(file);
        String username = authentication != null ? authentication.getName() : null;
        return ResponseUtil.success(SUCCESS_CREATE_DATA_MSG, articleUseCase.createArticle(request, username));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ArticleResponseDTO>> update(
            @PathVariable String id,
            @RequestPart("request") @Valid ArticleRequestDTO request,
            @RequestPart(value = "file", required = false) @ValidImage(required = false) MultipartFile file,
            Authentication authentication
    ) {
        request.setFile(file);
        String username = authentication != null ? authentication.getName() : null;
        return ResponseUtil.success(SUCCESS_UPDATE_DATA_MSG, articleUseCase.updateArticle(id, request, username));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        articleUseCase.deleteArticle(id);
        return ResponseUtil.success(SUCCESS_DELETE_DATA_MSG, null);
    }

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ArticleResponseDTO>> togglePublish(@PathVariable String id) {
        return ResponseUtil.success("Status publikasi artikel berhasil diubah", articleUseCase.togglePublish(id));
    }

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<java.util.Map<String, String>>> uploadImage(
            @RequestPart("file") @ValidImage MultipartFile file
    ) {
        String path = articleUseCase.uploadArticleImage(file);
        return ResponseUtil.success("Gambar artikel berhasil diunggah", java.util.Map.of("url", path));
    }
}
