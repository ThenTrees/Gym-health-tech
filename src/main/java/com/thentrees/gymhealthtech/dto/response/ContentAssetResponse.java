package com.thentrees.gymhealthtech.dto.response;

import com.thentrees.gymhealthtech.common.AssetType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentAssetResponse {
  private UUID id;
  private AssetType assetType;
  private String url;
  private String mimeType;
  private String sha256Hex;
  private Integer durationSeconds;
  private Integer width;
  private Integer height;
  private Long sizeBytes;
  private LocalDateTime createdAt;
}
