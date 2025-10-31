package com.thentrees.gymhealthtech.mapper;

import com.thentrees.gymhealthtech.dto.request.FoodRequest;
import com.thentrees.gymhealthtech.model.Food;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FoodMapper {
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "tags", source = "tags", ignore = true)
  void updateFoodFromRequest(FoodRequest request, @MappingTarget Food entity);

}
