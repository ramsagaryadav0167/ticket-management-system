package com.railbit.TicketManagementSystem.config;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.railbit.TicketManagementSystem.Entity.Notification;
import com.railbit.TicketManagementSystem.Entity.User;
import com.railbit.TicketManagementSystem.Service.NotificationService;
import com.railbit.TicketManagementSystem.Service.UserService;
import java.util.List;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalModelAttributeConfig {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    // 🔔 Yeh method har page ke model mein notifications daal dega
    @ModelAttribute
    public void addNotificationsToModel(Model model, Principal principal) {
        if (principal != null) {
            User currentUser = userService.findByUsername(principal.getName());
            List<Notification> notifications = notificationService.getUnreadNotificationsForUser(currentUser);
            model.addAttribute("notifications", notifications);
            model.addAttribute("notificationCount", notifications.size());
        }
    }
}