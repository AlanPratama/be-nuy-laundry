package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "articles")
@Getter
@Setter
public class ArticleEntity extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "category", length = 50, nullable = false)
    private String category;

    @Column(name = "excerpt", length = 500)
    private String excerpt;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "reading_time", length = 50)
    private String readingTime;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;

    @Column(name = "author_id", length = 36)
    private String authorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    private UserEntity author;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;
}
