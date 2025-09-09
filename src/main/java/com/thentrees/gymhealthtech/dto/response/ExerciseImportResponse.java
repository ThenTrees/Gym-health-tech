package com.thentrees.gymhealthtech.dto.response;

import com.thentrees.gymhealthtech.dto.helper.ImportError;
import com.thentrees.gymhealthtech.dto.helper.ImportSummary;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseImportResponse {
  private String status;
  private String message;
  private ImportSummary summary;
  private LocalDateTime processedAt;
  private List<ImportError> errors;

  public static ExerciseImportResponse success(ImportSummary summary) {
    ExerciseImportResponse response = new ExerciseImportResponse();
    response.status = "SUCCESS";
    response.message = "Exercises imported successfully";
    response.summary = summary;
    response.processedAt = LocalDateTime.now();
    response.errors = new ArrayList<>();
    return response;
  }

  public static ExerciseImportResponse partialSuccess(
      ImportSummary summary, List<ImportError> errors) {
    ExerciseImportResponse response = new ExerciseImportResponse();
    response.status = "PARTIAL_SUCCESS";
    response.message = "Some exercises imported with errors";
    response.summary = summary;
    response.processedAt = LocalDateTime.now();
    response.errors = errors;
    return response;
  }

  public static ExerciseImportResponse failure(String message, List<ImportError> errors) {
    ExerciseImportResponse response = new ExerciseImportResponse();
    response.status = "FAILURE";
    response.message = message;
    response.processedAt = LocalDateTime.now();
    response.errors = errors;
    return response;
  }
}
