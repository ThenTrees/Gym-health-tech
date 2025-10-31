package com.thentrees.gymhealthtech.mapper;

import com.thentrees.gymhealthtech.dto.request.CreateExerciseRequest;
import com.thentrees.gymhealthtech.dto.response.EquipmentTypeResponse;
import com.thentrees.gymhealthtech.dto.response.MuscleResponse;
import com.thentrees.gymhealthtech.model.Equipment;
import com.thentrees.gymhealthtech.model.Exercise;
import com.thentrees.gymhealthtech.model.Muscle;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ExerciseMapper {

  MuscleResponse toMuscleResponse(Muscle muscle);

  Muscle toMuscle(MuscleResponse muscleResponse);

  EquipmentTypeResponse toEquipmentTypeResponse(Equipment equipment);
}
