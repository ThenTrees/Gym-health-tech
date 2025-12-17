package com.thentrees.gymhealthtech.mapper;

import com.thentrees.gymhealthtech.dto.response.TemplateItemResponse;
import com.thentrees.gymhealthtech.dto.response.TemplateWorkoutDayResponse;
import com.thentrees.gymhealthtech.dto.response.TemplateWorkoutResponse;
import com.thentrees.gymhealthtech.model.TemplateDay;
import com.thentrees.gymhealthtech.model.TemplateItem;
import com.thentrees.gymhealthtech.model.WorkoutTemplate;
import java.util.Collections;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TemplateWorkoutMapper {

  @Mapping(target = "goal", expression = "java(template.getObjective() != null ? template.getObjective().toString() : null)")
  @Mapping(target = "totalWeek", source = "durationWeeks")
  @Mapping(target = "sessionPerWeek", source = "sessionsPerWeek")
  @Mapping(target = "totalExercise", expression = "java(calculateTotalExercise(template))")
  @Mapping(target = "templateWorkoutDay", expression = "java(mapTemplateDays(template.getTemplateDays()))")
  TemplateWorkoutResponse toResponse(WorkoutTemplate template);

  default List<TemplateWorkoutDayResponse> mapTemplateDays(List<TemplateDay> templateDays) {
    if (templateDays == null) {
      return Collections.emptyList();
    }
    return templateDays.stream()
        .map(this::toTemplateDayResponse)
        .toList();
  }

  @Mapping(target = "totalExercises", expression = "java(templateDay.getTemplateItems() != null ? templateDay.getTemplateItems().size() : 0)")
  @Mapping(target = "templateItems", expression = "java(mapTemplateItems(templateDay.getTemplateItems()))")
  TemplateWorkoutDayResponse toTemplateDayResponse(TemplateDay templateDay);

  default List<TemplateItemResponse> mapTemplateItems(List<TemplateItem> templateItems) {
    if (templateItems == null) {
      return Collections.emptyList();
    }
    return templateItems.stream()
        .map(this::toTemplateItemResponse)
        .toList();
  }

  @Mapping(target = "exerciseId", expression = "java(item.getExercise() != null ? item.getExercise().getId() : null)")
  @Mapping(target = "exerciseName", expression = "java(item.getExercise() != null ? item.getExercise().getName() : null)")
  @Mapping(target = "exerciseType", expression = "java(item.getExercise() != null && item.getExercise().getExerciseType() != null ? item.getExercise().getExerciseType().toString() : null)")
  @Mapping(target = "bodyPart", expression = "java(item.getExercise() != null ? item.getExercise().getBodyPart() : null)")
  @Mapping(target = "thumbnailUrl", expression = "java(item.getExercise() != null ? item.getExercise().getThumbnailUrl() : null)")
  TemplateItemResponse toTemplateItemResponse(TemplateItem item);

  default Integer calculateTotalExercise(WorkoutTemplate template) {
    if (template == null || template.getTemplateDays() == null) {
      return 0;
    }
    return template.getTemplateDays().stream()
        .mapToInt(day -> day.getTemplateItems() != null ? day.getTemplateItems().size() : 0)
        .sum();
  }
}

