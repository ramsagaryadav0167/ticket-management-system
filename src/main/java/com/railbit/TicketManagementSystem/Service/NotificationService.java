package com.railbit.TicketManagementSystem.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.railbit.TicketManagementSystem.Entity.Notification;
import com.railbit.TicketManagementSystem.Entity.User;
import com.railbit.TicketManagementSystem.Repository.NotificationRepository;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public void sendNotification(User recipient, String message) {
        Notification notification = new Notification(message, recipient);
        notification.setTimestamp(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    public List<Notification> getUnreadNotifications(User admin) {
        return notificationRepository.findByRecipientAndIsReadFalse(admin);
    }

    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id).orElse(null);
        if (notification != null) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }
    
    public List<Notification> getNotificationsForAdmin() {
        // Assuming only one admin for simplicity (username = "admin")
        User admin = notificationRepository.findAdminUser(); // ⛔ You’ll implement this in the repo
        if (admin != null) {
            return getUnreadNotifications(admin);
        }
        return List.of(); // return empty if admin not found
    }
    public List<Notification> getUnreadNotificationsForUser(User user) {
        return notificationRepository.findByRecipientAndIsReadFalse(user);
    }
    
}