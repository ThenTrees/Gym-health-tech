package com.thentrees.gymhealthtech.mapper;

import com.thentrees.gymhealthtech.dto.response.MuscleResponse;
import com.thentrees.gymhealthtech.model.Muscle;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MuscleMapper {
  MuscleResponse mapToResponse(Muscle muscle);
}
