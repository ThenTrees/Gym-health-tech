package com.thentrees.gymhealthtech.dto.response;

import lombok.Data;

@Data
public class SePayResponse {
  private String id;
  private String gateway; // ngan hang nao chuyen tien
  private String transactionDate;
  private String accountNumber; // so tai khoan da cau hinh trong sepay de nhan tien
  private String code;
  private String content;
  private String transferType = "in";
  private long transferAmount;
  private long accumulated;
  private String subAccount;
  private String referenceCode;
  private Object description;
}
