package com.thentrees.gymhealthtech.mapper;

import com.thentrees.gymhealthtech.dto.response.EquipmentTypeResponse;
import com.thentrees.gymhealthtech.dto.response.MuscleResponse;
import com.thentrees.gymhealthtech.model.EquipmentType;
import com.thentrees.gymhealthtech.model.Muscle;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExerciseMapper {

  MuscleResponse toMuscleResponse(Muscle muscle);

  EquipmentTypeResponse toEquipmentTypeResponse(EquipmentType equipmentType);
}
