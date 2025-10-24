package com.thentrees.gymhealthtech.service.impl;

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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

  private final DeviceTokenRepository deviceTokenRepository;
  private final UserService userService;
  private final NotificationRepository notificationRepository;

  @Transactional
  @Override
  public void savePushToken(PushTokenRequest request, UUID userId) {

    User user = userService.getUserById(userId);

    // Logic to save the push token associated with the user
    log.info("Saving push token for user {}: {}", user.getUsername(), request.getToken());
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
  public void sendPushNotification(SendNotificationRequest request)
      throws PushClientException, InterruptedException {

    List<DeviceToken> deviceTokens =
        deviceTokenRepository.findByUserId(UUID.fromString(request.getUserId()));

    for (DeviceToken deviceToken : deviceTokens) {
      if (!PushClient.isExponentPushToken(deviceToken.getPushToken()))
        throw new Error("Token:" + deviceToken.getPushToken() + " is not a valid token.");

      ExpoPushMessage expoPushMessage = new ExpoPushMessage();
      expoPushMessage.getTo().add(deviceToken.getPushToken());
      expoPushMessage.setTitle(request.getTitle());
      expoPushMessage.setBody(request.getBody());

      List<ExpoPushMessage> expoPushMessages = new ArrayList<>();
      expoPushMessages.add(expoPushMessage);

      PushClient client = new PushClient();
      List<List<ExpoPushMessage>> chunks = client.chunkPushNotifications(expoPushMessages);

      List<CompletableFuture<List<ExpoPushTicket>>> messageRepliesFutures = new ArrayList<>();

      for (List<ExpoPushMessage> chunk : chunks) {
        messageRepliesFutures.add(client.sendPushNotificationsAsync(chunk));
      }

      // Wait for each completable future to finish
      List<ExpoPushTicket> allTickets = new ArrayList<>();
      for (CompletableFuture<List<ExpoPushTicket>> messageReplyFuture : messageRepliesFutures) {
        try {
          for (ExpoPushTicket ticket : messageReplyFuture.get()) {
            allTickets.add(ticket);
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (ExecutionException e) {
          e.printStackTrace();
        }
      }

      List<ExpoPushMessageTicketPair<ExpoPushMessage>> zippedMessagesTickets =
          client.zipMessagesTickets(expoPushMessages, allTickets);

      List<ExpoPushMessageTicketPair<ExpoPushMessage>> okTicketMessages =
          client.filterAllSuccessfulMessages(zippedMessagesTickets);
      String okTicketMessagesString =
          okTicketMessages.stream()
              .map(p -> "Title: " + p.message.getTitle() + ", Id:" + p.ticket.getId())
              .collect(Collectors.joining(","));
      log.info(
          "Recieved OK ticket for "
              + okTicketMessages.size()
              + " messages: "
              + okTicketMessagesString);

      List<ExpoPushMessageTicketPair<ExpoPushMessage>> errorTicketMessages =
          client.filterAllMessagesWithError(zippedMessagesTickets);
      String errorTicketMessagesString =
          errorTicketMessages.stream()
              .map(
                  p ->
                      "Title: "
                          + p.message.getTitle()
                          + ", Error: "
                          + p.ticket.getDetails().getError())
              .collect(Collectors.joining(","));
      log.error(
          "Recieved ERROR ticket for "
              + errorTicketMessages.size()
              + " messages: "
              + errorTicketMessagesString);

      // Countdown 30s
      int wait = 5;
      for (int i = wait; i >= 0; i--) {
        System.out.print("Waiting for " + wait + " seconds. " + i + "s\r");
        Thread.sleep(1000);
      }
      log.info("Fetching reciepts...");

      List<String> ticketIds = (client.getTicketIdsFromPairs(okTicketMessages));
      CompletableFuture<List<ExpoPushReceipt>> receiptFutures =
          client.getPushNotificationReceiptsAsync(ticketIds);

      List<ExpoPushReceipt> receipts = new ArrayList<>();
      try {
        receipts = receiptFutures.get();
      } catch (ExecutionException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      log.info("Recieved " + receipts.size() + " receipts:");

      for (ExpoPushReceipt reciept : receipts) {
        log.info("Receipt for id: " + reciept.getId() + " had status: " + reciept.getStatus());
      }
    }
  }
}
