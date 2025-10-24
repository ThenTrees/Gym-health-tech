package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.Notification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {}
