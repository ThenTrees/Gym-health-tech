package com.thentrees.gymhealthtech.model;

import com.thentrees.gymhealthtech.common.AssetType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "content_assets")
public class ContentAsset extends BaseEntity {

  @Enumerated(EnumType.STRING)
  @Column(name = "asset_type", nullable = false)
  private AssetType assetType;

  @Column(name = "url", nullable = false, columnDefinition = "TEXT")
  private String url;

  @Column(name = "mime_type", length = 64)
  private String mimeType;

  @Column(name = "sha256_hex", length = 64)
  private String sha256Hex;

  @Column(name = "duration_s")
  private Integer durationSeconds;

  @Column(name = "width")
  private Integer width;

  @Column(name = "height")
  private Integer height;

  @Column(name = "size_bytes")
  private Long sizeBytes;
}
