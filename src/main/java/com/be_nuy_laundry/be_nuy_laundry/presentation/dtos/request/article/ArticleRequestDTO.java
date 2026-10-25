package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.request.article;

import com.be_nuy_laundry.be_nuy_laundry.domain.model.ArticleModel;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleRequestDTO {

    @NotBlank(message = "Judul artikel wajib diisi")
    private String title;

    @NotBlank(message = "Kategori artikel wajib diisi")
    private String category;

    private String excerpt;

    @NotBlank(message = "Konten artikel wajib diisi")
    private String content;

    private String readingTime;

    @Builder.Default
    private Boolean isPublished = false;

    private Boolean removeThumbnail;

    private MultipartFile file;

    public ArticleModel toModel() {
        return ArticleModel.builder()
                .title(this.title != null ? this.title.trim() : null)
                .category(this.category != null ? this.category.trim() : null)
                .excerpt(this.excerpt != null ? this.excerpt.trim() : null)
                .content(this.content != null ? this.content.trim() : null)
                .readingTime(this.readingTime != null ? this.readingTime.trim() : null)
                .isPublished(Boolean.TRUE.equals(this.isPublished))
                .build();
    }
}
