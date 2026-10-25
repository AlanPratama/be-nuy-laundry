package com.be_nuy_laundry.be_nuy_laundry.infrastructure.external.minio;

import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.MinIoRemoveObjectErrorResponse;
import io.minio.*;
import io.minio.messages.DeleteError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioAdapterTest {

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private MinioAdapter minioAdapter;

    private final String BUCKET_NAME = "nuy-laundry-test";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(minioAdapter, "bucketName", BUCKET_NAME);
    }

    @Test
    @DisplayName("givenBucketDoesNotExist_whenInit_thenCreateBucket")
    void givenBucketDoesNotExist_whenInit_thenCreateBucket() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        minioAdapter.init();

        verify(minioClient).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    @DisplayName("givenBucketAlreadyExists_whenInit_thenDoNotMakeBucket")
    void givenBucketAlreadyExists_whenInit_thenDoNotMakeBucket() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        minioAdapter.init();

        verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    @DisplayName("givenPolicyException_whenInit_thenCatchGracefully")
    void givenPolicyException_whenInit_thenCatchGracefully() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        doThrow(new RuntimeException("Policy failure")).when(minioClient).setBucketPolicy(any(SetBucketPolicyArgs.class));

        minioAdapter.init();

        verify(minioClient).setBucketPolicy(any(SetBucketPolicyArgs.class));
    }

    @Test
    @DisplayName("givenExceptionInInit_whenInit_thenThrowRuntimeException")
    void givenExceptionInInit_whenInit_thenThrowRuntimeException() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenThrow(new RuntimeException("Connection failed"));

        assertThatThrownBy(() -> minioAdapter.init())
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("givenFileBytes_whenUpload_thenPutObjectAndReturnFileName")
    void givenFileBytes_whenUpload_thenPutObjectAndReturnFileName() throws Exception {
        byte[] content = "imageBytesContent".getBytes();
        String fileName = "user/profile.png";
        String contentType = "image/png";

        String result = minioAdapter.upload(content, fileName, contentType);

        assertThat(result).isEqualTo(fileName);
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("givenMinioExceptionInUpload_whenUpload_thenThrowRuntimeException")
    void givenMinioExceptionInUpload_whenUpload_thenThrowRuntimeException() throws Exception {
        byte[] content = "imageBytesContent".getBytes();
        doThrow(new RuntimeException("Upload failed")).when(minioClient).putObject(any(PutObjectArgs.class));

        assertThatThrownBy(() -> minioAdapter.upload(content, "test.png", "image/png"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("givenFileName_whenDelete_thenInvokeRemoveObject")
    void givenFileName_whenDelete_thenInvokeRemoveObject() throws Exception {
        String fileName = "transactions/before.jpg";

        minioAdapter.delete(fileName);

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    @DisplayName("givenExceptionInDelete_whenDelete_thenThrowRuntimeException")
    void givenExceptionInDelete_whenDelete_thenThrowRuntimeException() throws Exception {
        doThrow(new RuntimeException("Delete failed")).when(minioClient).removeObject(any(RemoveObjectArgs.class));

        assertThatThrownBy(() -> minioAdapter.delete("file.jpg"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("givenFileName_whenDownload_thenReturnByteArray")
    void givenFileName_whenDownload_thenReturnByteArray() throws Exception {
        String fileName = "test-doc.pdf";
        byte[] expectedBytes = "documentContent".getBytes();
        GetObjectResponse mockResponse = mock(GetObjectResponse.class);
        when(mockResponse.readAllBytes()).thenReturn(expectedBytes);

        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        byte[] result = minioAdapter.download(fileName);

        assertThat(result).isEqualTo(expectedBytes);
    }

    @Test
    @DisplayName("givenExceptionInDownload_whenDownload_thenThrowRuntimeException")
    void givenExceptionInDownload_whenDownload_thenThrowRuntimeException() throws Exception {
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(new RuntimeException("Download failed"));

        assertThatThrownBy(() -> minioAdapter.download("test.pdf"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("givenFileNames_whenDeleteObjectsWithErrors_thenReturnErrorList")
    void givenFileNames_whenDeleteObjectsWithErrors_thenReturnErrorList() throws Exception {
        DeleteError error = mock(DeleteError.class);
        when(error.bucketName()).thenReturn(BUCKET_NAME);
        when(error.objectName()).thenReturn("file1.png");
        when(error.message()).thenReturn("Access Denied");

        @SuppressWarnings("unchecked")
        Result<DeleteError> errorResult = mock(Result.class);
        when(errorResult.get()).thenReturn(error);

        when(minioClient.removeObjects(any(RemoveObjectsArgs.class))).thenReturn(List.of(errorResult));

        List<MinIoRemoveObjectErrorResponse> errors = minioAdapter.deleteObjects(List.of("file1.png"));

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getObjectName()).isEqualTo("file1.png");
        assertThat(errors.get(0).getMessage()).isEqualTo("Access Denied");
    }

    @Test
    @DisplayName("givenFileNames_whenDeleteObjectsThrowsException_thenHandleSafely")
    void givenFileNames_whenDeleteObjectsThrowsException_thenHandleSafely() throws Exception {
        when(minioClient.removeObjects(any(RemoveObjectsArgs.class)))
                .thenThrow(new RuntimeException("Bulk remove failed"));

        List<MinIoRemoveObjectErrorResponse> errors = minioAdapter.deleteObjects(List.of("file1.png"));

        assertThat(errors).isEmpty();
    }
}
