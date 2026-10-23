package com.be_nuy_laundry.be_nuy_laundry.infrastructure.external.minio;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.username}")
    private String minIoUsername;

    @Value("${minio.password}")
    private String minIoPassword;

    @Value("${minio.url}")
    private String minIoURL;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minIoURL)
                .credentials(minIoUsername, minIoPassword)
                .build();
    }

}
