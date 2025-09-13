package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.common.VerificationType;
import com.thentrees.gymhealthtech.model.VerificationToken;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

  Optional<VerificationToken> findByTokenHashAndType(String tokenHash, VerificationType type);

  @Query(
      "SELECT vt FROM VerificationToken vt WHERE vt.user.id = :userId AND vt.type = :type AND vt.consumedAt IS NULL AND vt.expiresAt > :now")
  Optional<VerificationToken> findActiveTokenByUserAndType(
      @Param("userId") UUID userId,
      @Param("type") VerificationType type,
      @Param("now") OffsetDateTime now);

  void deleteByUserIdAndType(UUID userId, VerificationType type);
}
