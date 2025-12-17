package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.enums.DevicePlatform;
import com.thentrees.gymhealthtech.model.DeviceToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {
  List<DeviceToken> findByUserId(UUID userId);

  @Query("SELECT dt FROM DeviceToken dt WHERE dt.user.id = :userId AND dt.platform = :platform AND dt.enabled = true")
  Optional<DeviceToken> findByUserAndPlatform(@Param("userId") UUID userId, @Param("platform") DevicePlatform platform);

}
