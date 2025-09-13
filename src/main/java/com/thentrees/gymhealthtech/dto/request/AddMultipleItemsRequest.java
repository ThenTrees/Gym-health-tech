package com.thentrees.gymhealthtech.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddMultipleItemsRequest {

  @Valid
  @NotEmpty(message = "Danh sách bài tập không được trống")
  private List<CreateCustomPlanItemRequest> planItems;
}
