package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.RefreshToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

  @Query("SELECT rt FROM RefreshToken rt WHERE rt.revokedAt IS NULL AND rt.expiresAt > :now")
  List<RefreshToken> findActiveTokens(@Param("now") LocalDateTime now);

  default List<RefreshToken> findActiveTokens() {
    return findActiveTokens(LocalDateTime.now());
  }

  @Modifying
  @Query(
      "UPDATE RefreshToken rt SET rt.revokedAt = :now WHERE rt.user.id = :userId AND rt.revokedAt IS NULL")
  void revokeAllByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

  default void revokeAllByUserId(UUID userId) {
    revokeAllByUserId(userId, LocalDateTime.now());
  }

  @Query(
      "SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revokedAt IS NULL AND rt.expiresAt > :now")
  List<RefreshToken> findActiveTokensByUserId(
      @Param("userId") UUID userId, @Param("now") LocalDateTime now);

  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
  void deleteExpiredTokens(@Param("now") LocalDateTime now);
}
