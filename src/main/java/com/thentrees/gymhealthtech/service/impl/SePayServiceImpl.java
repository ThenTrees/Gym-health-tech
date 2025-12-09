package com.thentrees.gymhealthtech.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thentrees.gymhealthtech.dto.response.SePayResponse;
import com.thentrees.gymhealthtech.enums.PaymentStatus;
import com.thentrees.gymhealthtech.model.Payment;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.repository.PaymentRepository;
import com.thentrees.gymhealthtech.repository.UserRepository;
import com.thentrees.gymhealthtech.service.SePayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j(topic = "SEPAY-SERVICE")
@RequiredArgsConstructor
public class SePayServiceImpl implements SePayService {
  private final PaymentRepository paymentRepository;
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;

  @Value("${app.sepay.bankAccount:0938749250}")
  private String bankAccount;

  @Value("${app.sepay.bankName:MB}")
  private String bankName;

  public static final long PREMIUM_AMOUNT = 10000L;

  @Transactional
  @Override
  public String createPendingTransaction(Authentication authentication) {

    User user = (User) authentication.getPrincipal();

    String code = String.format("SUB-%s-%s", user.getId().toString(), UUID.randomUUID().toString().substring(0,8));
    code = code.replace("-", "");
    Payment tx = Payment.builder()
      .user(user)
      .amount(PREMIUM_AMOUNT)
      .provider("SEPAY")
      .status(PaymentStatus.PENDING)
      .providerTxnId(code)
      .transactionCode(code)
      .build();

       paymentRepository.save(tx);
    return String.format("https://qr.sepay.vn/img?acc=%s&bank=%s&&amount=%s&des=%s",
      bankAccount,
      bankName,
      PREMIUM_AMOUNT,
      code
    );
  }

  @Transactional
  @Override
  public boolean handleWebhook(SePayResponse payload) {
    log.info("Received SePay webhook: {}", payload);

    String desc = payload.getContent();
    if (desc == null || !desc.contains("-")) {
      return false;
    }

    // Extract transaction code safely
    String[] parts = desc.split("-");
    if (parts.length < 2) return false;
    String txnCode = parts[1];

    Payment tx = paymentRepository.findByTransactionCode(txnCode)
      .orElse(null);
    if (tx == null) return false;

    long paidAmount = parseAmount(payload.getTransferAmount());
    tx.setRawResponse(toJsonSafe(payload));

    boolean isCorrectAmount = paidAmount == tx.getAmount();

    // Update status
    tx.setStatus(isCorrectAmount ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
    paymentRepository.save(tx);

    if (!isCorrectAmount) return false;

    // Activate premium
    activatePremium(tx.getUser().getId());

    return true;
  }

  @Override
  public boolean checkPremiumStatus(Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    Optional<User> userOpt = userRepository.findById(user.getId());
    return userOpt.map(User::getIsPremium).orElse(false);
  }

  private long parseAmount(Object amountObj) {
    if (amountObj instanceof Number n) {
      return n.longValue();
    }
    try {
      return Long.parseLong(amountObj.toString());
    } catch (Exception e) {
      return 0L;
    }
  }

  private String toJsonSafe(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize SePayResponse", e);
      return "{}";
    }
  }

  private void activatePremium(UUID userId) {
    userRepository.findById(userId).ifPresent(user -> {
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime newExpiry = (user.getPremiumExpiresAt() != null
        && user.getPremiumExpiresAt().isAfter(now))
        ? user.getPremiumExpiresAt().plusDays(30)
        : now.plusDays(30);

      user.setIsPremium(true);
      user.setPremiumExpiresAt(newExpiry);
      userRepository.save(user);
    });
  }

}
