package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.Food;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodRepository extends JpaRepository<Food, UUID> {

  Page<Food> findAllByIsActiveTrue(Pageable pageable);

  Optional<Food> findByIdAndIsActiveTrue(UUID id);
  @Query("SELECT f FROM Food f WHERE f.mealTime LIKE %:mealTime% AND f.isActive = true")
  List<Food> findByMealTimeContaining(@Param("mealTime") String mealTime);

  List<Food> findByCategoryAndIsActiveTrue(String category);

  @Query(
      "SELECT f FROM Food f WHERE (LOWER(f.foodNameVi) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND f.isActive = true")
  Page<Food> findAllByFoodNameVi(@Param("keyword") String keyword, Pageable pageable);
}
