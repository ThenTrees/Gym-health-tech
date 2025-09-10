package com.thentrees.gymhealthtech.dto.helper;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ImportSummary {
  private int totalExercises;
  private int successfulImports;
  private int failedImports;
  private int duplicatesSkipped;
  private int categoriesCreated;
  private long processingTimeMs;

  // Constructors, getters, setters
  public ImportSummary(int total, int success, int failed, int skipped, int categories, long time) {
    this.totalExercises = total;
    this.successfulImports = success;
    this.failedImports = failed;
    this.duplicatesSkipped = skipped;
    this.categoriesCreated = categories;
    this.processingTimeMs = time;
  }

  // Getters and setters
}
