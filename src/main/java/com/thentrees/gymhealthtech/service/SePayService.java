package com.thentrees.gymhealthtech.service;

import com.thentrees.gymhealthtech.dto.response.SePayResponse;
import org.springframework.security.core.Authentication;

public interface SePayService {

  String createPendingTransaction(Authentication authentication);
  boolean handleWebhook(SePayResponse payload);

}
