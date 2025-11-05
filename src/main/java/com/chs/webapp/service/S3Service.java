package com.chs.webapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class S3Service {

    private final S3Client s3Client;
    private final String bucketName;

    public S3Service(S3Client s3Client, @Value("${aws.s3.bucket-name}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    /**
     * 上傳檔案到 S3
     * @param file 要上傳的檔案
     * @param userId 使用者 ID (用於分區儲存)
     * @param productId 產品 ID
     * @return S3 物件的完整路徑
     */
    public String uploadFile(MultipartFile file, UUID userId, UUID productId) {
        try {
            // 生成唯一的 S3 key: userId/productId/timestamp-originalFilename
            String timestamp = String.valueOf(System.currentTimeMillis());
            String originalFilename = file.getOriginalFilename();
            String s3Key = String.format("%s/%s/%s-%s", userId, productId, timestamp, originalFilename);

            log.info("Uploading file to S3: bucket={}, key={}", bucketName, s3Key);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            log.info("File uploaded successfully to S3: {}", s3Key);
            return s3Key;

        } catch (S3Exception e) {
            log.error("Error uploading file to S3: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("Error reading file: {}", e.getMessage());
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        }
    }

    /**
     * 從 S3 刪除檔案
     * @param s3Key S3 物件的 key
     */
    public void deleteFile(String s3Key) {
        try {
            log.info("Deleting file from S3: bucket={}, key={}", bucketName, s3Key);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

            log.info("File deleted successfully from S3: {}", s3Key);

        } catch (S3Exception e) {
            log.error("Error deleting file from S3: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Failed to delete file from S3: " + e.getMessage(), e);
        }
    }

    /**
     * 檢查檔案是否存在於 S3
     * @param s3Key S3 物件的 key
     * @return 檔案是否存在
     */
    public boolean fileExists(String s3Key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("Error checking file existence in S3: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Failed to check file existence: " + e.getMessage(), e);
        }
    }
}