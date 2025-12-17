package com.thentrees.gymhealthtech.util;

import static com.thentrees.gymhealthtech.constant.S3Constant.*;

import com.thentrees.gymhealthtech.exception.BusinessException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3Util {

  private final S3Client s3Client;

  @Value("${aws.s3.bucket}")
  private String bucketName;

  /** Upload file lên S3 và trả về public URL */
  public String uploadFile(MultipartFile file, String directory) {
    String s3Key = generateFileName(file.getOriginalFilename(), directory);
    try {
      PutObjectRequest putObjectRequest =
          PutObjectRequest.builder()
              .bucket(bucketName)
              .key(s3Key)
              .contentType(file.getContentType())
              .contentLength(file.getSize())
              .build();
      try (InputStream inputStream = file.getInputStream()) {
        s3Client.putObject(
            putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
      }
      return s3Key;
    } catch (S3Exception e) {
      log.error("AWS S3 error: {}", e.awsErrorDetails().errorMessage());
      throw new BusinessException("Failed to upload image to S3");
    } catch (IOException e) {
      log.error("I/O error when uploading file: {}", e.getMessage(), e);
      throw new BusinessException("Failed to read file input stream", e.getMessage());
    } catch (Exception e) {
      log.error("Error uploading file to S3: {}", e.getMessage(), e);
      throw new BusinessException("Failed to upload file to S3", e.getMessage());
    }
  }

  /** Xóa file khỏi S3 bằng URL hoặc key */
  public void deleteFileByUrl(String fileUrl) {
    if (fileUrl == null || !fileUrl.contains(bucketName)) {
      log.warn("Invalid file URL: {}", fileUrl);
      return;
    }

    String key = extractKeyFromUrl(fileUrl);
    deleteFileByKey(key);
  }

  public void deleteFileByKey(String key) {
    try {
      DeleteObjectRequest request =
          DeleteObjectRequest.builder().bucket(bucketName).key(key).build();

      s3Client.deleteObject(request);
      log.info("Deleted file from S3: {}", key);
    } catch (Exception e) {
      log.error("Failed to delete file from S3: {}", key, e);
    }
  }

  public String generateFileName(String originalFileName, String directory) {
    String timestamp = String.valueOf(System.currentTimeMillis());
    String randomUUID = UUID.randomUUID().toString();
    String extension = getFileExtension(originalFileName);

    return directory + "/" + timestamp + "_" + randomUUID + extension;
  }

  public String getFileExtension(String fileName) {
    if (fileName == null || !fileName.contains(".")) {
      return "";
    }
    return fileName.substring(fileName.lastIndexOf("."));
  }

  public String getFileUrl(String key) {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("S3 object key must not be null or empty");
    }

    return String.format(
        S3_URL_TEMPLATE, bucketName, s3Client.serviceClientConfiguration().region().id(), key);
  }

  private String extractKeyFromUrl(String fileUrl) {
    if (fileUrl == null) return null;
    int index = fileUrl.indexOf(".amazonaws.com/");
    return index == -1 ? fileUrl : fileUrl.substring(index + ".amazonaws.com/".length());
  }
}
