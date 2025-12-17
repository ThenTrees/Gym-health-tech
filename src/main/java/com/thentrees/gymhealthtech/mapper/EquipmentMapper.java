package com.thentrees.gymhealthtech.mapper;

import com.thentrees.gymhealthtech.dto.response.EquipmentResponse;
import com.thentrees.gymhealthtech.model.Equipment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EquipmentMapper {

  EquipmentResponse toResponse(Equipment equipment);
}

