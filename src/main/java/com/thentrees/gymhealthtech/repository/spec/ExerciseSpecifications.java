package com.thentrees.gymhealthtech.repository.spec;

import com.thentrees.gymhealthtech.common.ExerciseLevel;
import com.thentrees.gymhealthtech.model.*;
import jakarta.persistence.criteria.Join;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/** Specifications for filtering Exercise entities based on various criteria. */
public class ExerciseSpecifications {
  public static Specification<Exercise> hasKeyword(String keyword) {
    return (root, query, criteriaBuilder) -> {
      if (keyword == null || keyword.trim().isEmpty()) {
        return criteriaBuilder.conjunction();
      }

      String likeKeyword = "%" + keyword.toLowerCase() + "%";
      return criteriaBuilder.or(
          criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likeKeyword),
          criteriaBuilder.like(criteriaBuilder.lower(root.get("instructions")), likeKeyword));
    };
  }

  public static Specification<Exercise> hasLevel(ExerciseLevel level) {
    return (root, query, criteriaBuilder) -> {
      if (level == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(root.get("level"), level);
    };
  }

  public static Specification<Exercise> hasPrimaryMuscle(String muscleCode) {
    return (root, query, criteriaBuilder) -> {
      if (muscleCode == null || muscleCode.trim().isEmpty()) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(root.get("primaryMuscle").get("code"), muscleCode);
    };
  }

  public static Specification<Exercise> hasEquipment(String equipmentCode) {
    return (root, query, criteriaBuilder) -> {
      if (equipmentCode == null || equipmentCode.trim().isEmpty()) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(root.get("equipment").get("code"), equipmentCode);
    };
  }

  public static Specification<Exercise> hasMuscles(List<String> muscleCodes) {
    return (root, query, criteriaBuilder) -> {
      if (muscleCodes == null || muscleCodes.isEmpty()) {
        return criteriaBuilder.conjunction();
      }

      Join<Exercise, ExerciseMuscle> muscleJoin = root.join("exerciseMuscles");
      Join<ExerciseMuscle, Muscle> muscle = muscleJoin.join("muscle");

      return muscle.get("code").in(muscleCodes);
    };
  }

  public static Specification<Exercise> hasExerciseType(String exerciseTypeCode) {
    return (root, query, criteriaBuilder) -> {
      if (exerciseTypeCode == null || exerciseTypeCode.trim().isEmpty()) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(root.get("exerciseType").get("code"), exerciseTypeCode);
    };
  }
}
