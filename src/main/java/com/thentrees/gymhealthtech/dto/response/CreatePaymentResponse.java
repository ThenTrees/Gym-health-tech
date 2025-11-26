package com.thentrees.gymhealthtech.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentResponse {
  private String bankAccount;
  private String bankName;
  private String owner;
  private String qrImageBase64; // optional
  private String transferContent;
  private Long amount;
}
