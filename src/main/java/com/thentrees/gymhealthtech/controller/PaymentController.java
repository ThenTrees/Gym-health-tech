package com.thentrees.gymhealthtech.controller;

import com.thentrees.gymhealthtech.constant.AppConstants;
import com.thentrees.gymhealthtech.dto.response.APIResponse;
import com.thentrees.gymhealthtech.dto.response.CheckPremiumResponse;
import com.thentrees.gymhealthtech.dto.response.SePayResponse;
import com.thentrees.gymhealthtech.service.SePayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstants.API_V1 + "/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final SePayService sePayService;

  // Create payment info for frontend
  @PostMapping("/create")
  public ResponseEntity<String> createPayment(Authentication authentication) {
    String imageUrl = sePayService.createPendingTransaction(authentication);
    return ResponseEntity.ok(imageUrl);
  }

  // SePay webhook
  @PostMapping("/sep-pay-webhook")
  public ResponseEntity<String> sepPayWebhook(@RequestBody SePayResponse payload) {
    boolean ok = sePayService.handleWebhook(payload);
    if (ok) return ResponseEntity.ok("OK");
    return ResponseEntity.badRequest().body("IGNORED");
  }

  // check premium status
  @GetMapping("/status")
  public ResponseEntity<APIResponse<CheckPremiumResponse>> checkPremiumStatus(Authentication authentication) {
    boolean isPremium = sePayService.checkPremiumStatus(authentication);
    CheckPremiumResponse checkPremiumResponse = new CheckPremiumResponse();
    checkPremiumResponse.setPremium(isPremium);
    return ResponseEntity.ok(APIResponse.success(checkPremiumResponse));
  }
}
