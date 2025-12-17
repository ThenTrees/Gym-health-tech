package com.thentrees.gymhealthtech.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MonthlySummaryResponse {
  private LocalDate month; // ví dụ: 2025-11
  // ===== Tổng quan toàn tháng =====
  private int totalSessions;
  private int completedSessions;
  private double completionRate; // (%)

  private int totalSets;
  private int completedSets;
  private double avgCompletionPercentage;

  private int totalVolume; // Tổng khối lượng (weight * reps)
  private double avgVolumePerSession;

  private int totalDurationMinutes;
  private double avgDurationPerSession;

  private double avgRpe; // Mức độ nỗ lực trung bình (1–10)

  // ===== Phân tích theo tuần =====
  private List<SessionResponse> weeklySummaries;

  // ===== Gợi ý / nhận xét =====
  private String feedback;
}
