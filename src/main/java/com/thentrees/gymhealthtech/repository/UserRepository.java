package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

  @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile WHERE u.email = :email")
  Optional<User> findByEmail(@Param("email") String email);

  Optional<User> findByPhone(String phone);

  boolean existsByEmail(String email);

  boolean existsByPhone(String phone);

  @Query(
      "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.profile WHERE (u.email = :identifier OR u.phone = :identifier) AND u.status = 'ACTIVE'")
  Optional<User> findByEmailOrPhone(@Param("identifier") String identifier);

  @Query("""
    SELECT DISTINCT p.user
    FROM Plan p
    JOIN p.planDays d
    WHERE d.scheduledDate = :date
    """)
  List<User> findUsersWithWorkoutOnDate(@Param("date") LocalDate date);
}
