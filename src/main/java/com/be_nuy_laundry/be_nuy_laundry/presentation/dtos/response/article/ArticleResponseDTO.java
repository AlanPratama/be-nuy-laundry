package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.article;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleResponseDTO {
    private String id;
    private String title;
    private String slug;
    private String category;
    private String excerpt;
    private String content;
    private String readingTime;
    private String thumbnailUrl;
    private Boolean isPublished;
    private String authorId;
    private String authorName;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
