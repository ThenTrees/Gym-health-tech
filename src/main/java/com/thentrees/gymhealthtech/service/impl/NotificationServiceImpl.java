package com.thentrees.gymhealthtech.service.impl;

import com.thentrees.gymhealthtech.constant.ErrorMessages;
import com.thentrees.gymhealthtech.dto.request.PushTokenRequest;
import com.thentrees.gymhealthtech.dto.request.SendNotificationRequest;
import com.thentrees.gymhealthtech.enums.DevicePlatform;
import com.thentrees.gymhealthtech.enums.NotificationType;
import com.thentrees.gymhealthtech.model.DeviceToken;
import com.thentrees.gymhealthtech.model.Notification;
import com.thentrees.gymhealthtech.model.User;
import com.thentrees.gymhealthtech.repository.DeviceTokenRepository;
import com.thentrees.gymhealthtech.repository.NotificationRepository;
import com.thentrees.gymhealthtech.service.NotificationService;
import com.thentrees.gymhealthtech.service.UserService;
import io.github.jav.exposerversdk.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "NOTIFICATION-SERVICE")
public class NotificationServiceImpl implements NotificationService {

  private final DeviceTokenRepository deviceTokenRepository;
  private final UserService userService;
  private final NotificationRepository notificationRepository;

  @Override
  public void savePushToken(PushTokenRequest request, Authentication authentication) {
    User user = (User) authentication.getPrincipal();

    DeviceToken deviceToken =
        DeviceToken.builder()
            .user(user)
            .pushToken(request.getToken())
            .platform(DevicePlatform.valueOf(request.getPlatform().toUpperCase()))
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .build();

    deviceTokenRepository.save(deviceToken);
  }

  @Transactional
  @Override
  public void sendPushNotification(SendNotificationRequest request) throws PushClientException {

    // Retrieve device tokens for the user
    List<DeviceToken> deviceTokens =
        deviceTokenRepository.findByUserId(UUID.fromString(request.getUserId()));

    if (deviceTokens.isEmpty()) {
      log.warn("No device tokens found for user {}", request.getUserId());
      return;
    }

    // Chuẩn bị danh sách message
    List<ExpoPushMessage> messages =
        deviceTokens.stream()
            .filter(token -> PushClient.isExponentPushToken(token.getPushToken()))
            .map(token -> createMessage(token.getPushToken(), request))
            .collect(Collectors.toList());

    PushClient client = new PushClient();
    List<ExpoPushTicket> tickets = sendMessages(client, messages);
    Notification notification =
        Notification.builder()
            .receiverId(UUID.fromString(request.getUserId()))
            .title(request.getTitle())
            .content(request.getBody())
            .type(NotificationType.valueOf(request.getNotificationType()))
            .createdAt(LocalDateTime.now())
            .build();
    notificationRepository.save(notification);
    processTickets(client, messages, tickets);

    // (Optional) Lấy receipts để kiểm tra tình trạng
    fetchAndLogReceipts(client, tickets);
  }

  private ExpoPushMessage createMessage(String pushToken, SendNotificationRequest request) {
    ExpoPushMessage message = new ExpoPushMessage();
    message.getTo().add(pushToken);
    message.setTitle(request.getTitle());
    message.setBody(request.getBody());
    return message;
  }

  private List<ExpoPushTicket> sendMessages(PushClient client, List<ExpoPushMessage> messages) {

    List<List<ExpoPushMessage>> chunks = client.chunkPushNotifications(messages);
    List<CompletableFuture<List<ExpoPushTicket>>> futures = new ArrayList<>();

    for (List<ExpoPushMessage> chunk : chunks) {
      futures.add(client.sendPushNotificationsAsync(chunk));
    }

    return futures.stream()
        .map(
            future -> {
              try {
                return future.get();
              } catch (Exception e) {
                log.error(ErrorMessages.SEND_NOTIFICATION_ERROR, e);
                return Collections.<ExpoPushTicket>emptyList();
              }
            })
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  /** Log kết quả */
  private void processTickets(
      PushClient client, List<ExpoPushMessage> messages, List<ExpoPushTicket> tickets) {
    List<ExpoPushMessageTicketPair<ExpoPushMessage>> pairs =
        client.zipMessagesTickets(messages, tickets);

    List<ExpoPushMessageTicketPair<ExpoPushMessage>> success =
        client.filterAllSuccessfulMessages(pairs);
    if (!success.isEmpty()) {
      log.info("Sent {} push notifications successfully", success.size());
    }

    List<ExpoPushMessageTicketPair<ExpoPushMessage>> errors =
        client.filterAllMessagesWithError(pairs);
    if (!errors.isEmpty()) {
      String errorMessages =
          errors.stream()
              .map(p -> String.valueOf(p.ticket.getDetails().getError()))
              .collect(Collectors.joining(", "));
      log.error("{} notifications failed: {}", errors.size(), errorMessages);
    }
  }

  /** Xử lý receipt riêng */
  private void fetchAndLogReceipts(PushClient client, List<ExpoPushTicket> tickets) {

    List<String> ticketIds =
        tickets.stream()
            .map(ExpoPushTicket::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    if (ticketIds.isEmpty()) return;

    log.info("Fetching push receipts...");
    CompletableFuture<List<ExpoPushReceipt>> receiptsFuture =
        client.getPushNotificationReceiptsAsync(ticketIds);

    try {
      List<ExpoPushReceipt> receipts = receiptsFuture.get();
      for (ExpoPushReceipt receipt : receipts) {
        log.info("📩 Receipt {} - Status: {}", receipt.getId(), receipt.getStatus());
      }
    } catch (Exception e) {
      log.error("Failed to fetch receipts", e);
    }
  }

  @Override
  public boolean isUserSubscribed(Authentication authentication, String platform) {
    User user = (User) authentication.getPrincipal();
    DevicePlatform devicePlatform = DevicePlatform.valueOf(platform.toUpperCase());
    return deviceTokenRepository.findByUserAndPlatform(user.getId(), devicePlatform).isPresent();
  }

}
