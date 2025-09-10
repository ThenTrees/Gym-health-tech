package com.thentrees.gymhealthtech.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImportMetadata {
  private String source;
  private String version;
  private LocalDateTime exportDate;
  private Integer totalCount;
}
