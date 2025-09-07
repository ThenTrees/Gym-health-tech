package com.thentrees.gymhealthtech.dto.response;

import com.thentrees.gymhealthtech.common.AssetType;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseAssetResponse {
  private UUID assetId;
  private AssetType assetType;
  private String url;
  private String mimeType;
  private Integer durationSeconds;
  private Integer width;
  private Integer height;
  private Long sizeBytes;
  private Integer sortOrder;
}
