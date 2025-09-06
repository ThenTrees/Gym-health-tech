package com.thentrees.gymhealthtech.util;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

@Component
public class S3Util {
  @Autowired private S3Client s3Client;

  @Value("${aws.s3.bucket}")
  private String bucketName;

  public String generateFileName(String originalFileName) {
    String timestamp = String.valueOf(System.currentTimeMillis());
    String randomUUID = UUID.randomUUID().toString();
    String extension = getFileExtension(originalFileName);

    return "avatars/" + timestamp + "_" + randomUUID + extension;
  }

  public String getFileExtension(String fileName) {
    if (fileName == null || !fileName.contains(".")) {
      return "";
    }
    return fileName.substring(fileName.lastIndexOf("."));
  }

  public String getFileUrl(String key) {
    return String.format(
        "https://%s.s3.%s.amazonaws.com/%s",
        bucketName, s3Client.serviceClientConfiguration().region().id(), key);
  }

  public boolean isValidImageFile(MultipartFile file) {
    String contentType = file.getContentType();
    return contentType != null && contentType.startsWith("image/");
  }
}
