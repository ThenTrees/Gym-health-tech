package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.common.AssetType;
import com.thentrees.gymhealthtech.model.ContentAsset;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentAssetRepository extends JpaRepository<ContentAsset, UUID> {

  @Query("SELECT ca FROM ContentAsset ca WHERE ca.id IN :ids ORDER BY ca.createdAt")
  List<ContentAsset> findByIdsOrdered(@Param("ids") List<UUID> ids);

  List<ContentAsset> findByAssetTypeOrderByCreatedAtDesc(AssetType assetType);

  @Query(
      "SELECT ca FROM ContentAsset ca "
          + "WHERE (:assetType IS NULL OR ca.assetType = :assetType) "
          + "AND (:mimeType IS NULL OR ca.mimeType LIKE CONCAT(:mimeType, '%')) "
          + "ORDER BY ca.createdAt DESC")
  Page<ContentAsset> findAssetsWithFilters(
      @Param("assetType") AssetType assetType,
      @Param("mimeType") String mimeType,
      Pageable pageable);
}
