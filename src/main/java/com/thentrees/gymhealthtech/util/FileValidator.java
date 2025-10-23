package com.thentrees.gymhealthtech.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileValidator {

  private static final long MAX_IMAGE_FILE_SIZE = 5 * 1024 * 1024; // 5MB
  private static final long MAX_VIDEO_FILE_SIZE = 50 * 1024 * 1024; // 50MB

  /** Kiểm tra xem file có hợp lệ là ảnh không */
  public void validateImage(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is empty");
    }

    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new IllegalArgumentException("Only image files are allowed");
    }

    if (file.getSize() > MAX_IMAGE_FILE_SIZE) {
      throw new IllegalArgumentException("File size must be less than 5MB");
    }
  }

  /** (Optional) Validate file video */
  public void validateVideo(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is empty");
    }

    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("video/")) {
      throw new IllegalArgumentException("Only video files are allowed");
    }

    if (file.getSize() > MAX_VIDEO_FILE_SIZE) { // 50MB
      throw new IllegalArgumentException("Video file size must be less than 50MB");
    }
  }
}
