package com.thentrees.gymhealthtech.mapper;

import com.thentrees.gymhealthtech.dto.request.FoodRequest;
import com.thentrees.gymhealthtech.dto.response.FoodResponse;
import com.thentrees.gymhealthtech.model.Food;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FoodMapper {

  FoodResponse toResponse(Food food);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "tags", source = "tags", ignore = true)
  void updateFoodFromRequest(FoodRequest request, @MappingTarget Food entity);
}
