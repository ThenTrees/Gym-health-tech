package com.thentrees.gymhealthtech.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadResponse {
  private String message;
  private String url;
  private String filename;
  private long size;
}
