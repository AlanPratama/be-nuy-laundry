package com.be_nuy_laundry.be_nuy_laundry.infrastructure.external.minio;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.out.FileStoragePort;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.MinIoRemoveObjectErrorResponse;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MinioAdapter implements FileStoragePort {

    @Value("${minio.bucketName}")
    private String bucketName;

    private final MinioClient minioClient;

    @PostConstruct
    public void init() {
        try {
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());

            if (!found) {
//                minioClient.setBucketPolicy(
//                        SetBucketPolicyArgs.builder()
//                                .bucket(bucketName)
//                                .config("""
//                {
//                  "Version": "2012-10-17",
//                  "Statement": [
//                    {
//                      "Effect": "Allow",
//                      "Principal": "*",
//                      "Action": ["s3:GetObject"],
//                      "Resource": ["arn:aws:s3:::%s/*"]
//                    }
//                  ]
//                }
//                """.formatted(bucketName))
//                                .build()
//                );


                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String upload(byte[] file, String fileName, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(new ByteArrayInputStream(file), file.length, -1)
                            .contentType(contentType)
                            .build()
            );

            return  fileName;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<MinIoRemoveObjectErrorResponse> deleteObjects(List<String> fileNames) {
        List<MinIoRemoveObjectErrorResponse> minIoRemoveObjectErrorResponses = new ArrayList<>();
        List<DeleteObject> objects = new ArrayList<>();

        for (String fileName : fileNames)
            objects.add(new DeleteObject(fileName));

        try {

            Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(bucketName)
                            .objects(objects)
                            .build());

            for (Result<DeleteError> errorResult : results) {
                DeleteError error = errorResult.get();

                MinIoRemoveObjectErrorResponse minIoRemoveObjectErrorResponse = new MinIoRemoveObjectErrorResponse();
                minIoRemoveObjectErrorResponse.setBucketName(error.bucketName());
                minIoRemoveObjectErrorResponse.setObjectName(error.objectName());
                minIoRemoveObjectErrorResponse.setMessage(error.message());

                minIoRemoveObjectErrorResponses.add(minIoRemoveObjectErrorResponse);
            }

        } catch (Exception e) {
            e.fillInStackTrace();
        }

        return minIoRemoveObjectErrorResponses;
    }

    @Override
    public byte[] download(String fileName) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        )) {
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
