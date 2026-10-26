package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleMetricsDTO {
    private long totalArticles;
    private long publishedArticles;
    private long draftArticles;
}
