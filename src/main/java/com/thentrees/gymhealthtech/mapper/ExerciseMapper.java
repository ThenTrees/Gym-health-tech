package com.thentrees.gymhealthtech.mapper;

import com.thentrees.gymhealthtech.dto.response.EquipmentTypeResponse;
import com.thentrees.gymhealthtech.dto.response.ExerciseDetailResponse;
import com.thentrees.gymhealthtech.dto.response.MuscleResponse;
import com.thentrees.gymhealthtech.model.Equipment;
import com.thentrees.gymhealthtech.model.Exercise;
import com.thentrees.gymhealthtech.model.ExerciseMuscle;
import com.thentrees.gymhealthtech.model.Muscle;
import java.util.Collections;
import java.util.List;
import org.mapstruct.*;

@Mapper(
  componentModel = "spring",
  imports = {List.class, Collections.class}
)
public interface ExerciseMapper {

  MuscleResponse toMuscleResponse(Muscle muscle);

  Muscle toMuscle(MuscleResponse muscleResponse);

  EquipmentTypeResponse toEquipmentTypeResponse(Equipment equipment);

  @Mapping(target = "primaryMuscle", expression = "java(exercise.getPrimaryMuscle().getName())")
  @Mapping(target = "equipment", expression = "java(exercise.getEquipment() != null ? exercise.getEquipment().getName() : null)")
  @Mapping(target = "exerciseCategory", expression = "java(exercise.getExerciseCategory() != null ? exercise.getExerciseCategory().getName() : null)")
  @Mapping(target = "exerciseType", expression = "java(exercise.getExerciseType() != null ? exercise.getExerciseType().name() : null)")
  @Mapping(target = "secondaryMuscles", expression = "java(extractSecondaryMuscles(exercise))")
  @Mapping(target = "instructions", ignore = true)
  @Mapping(target = "level", ignore = true)
  ExerciseDetailResponse toExerciseDetailResponse(Exercise exercise);

  default List<String> extractSecondaryMuscles(Exercise exercise) {
    if (exercise.getExerciseMuscles() == null) {
      return Collections.emptyList();
    }
    return exercise.getExerciseMuscles().stream()
        .map(ExerciseMuscle::getMuscle)
        .map(Muscle::getName)
        .toList();
  }
}
