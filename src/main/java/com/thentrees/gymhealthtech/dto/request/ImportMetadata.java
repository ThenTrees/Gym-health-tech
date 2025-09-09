package com.thentrees.gymhealthtech.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImportMetadata {
  private String source;
  private String version;
  private LocalDateTime exportDate;
  private Integer totalCount;

}
