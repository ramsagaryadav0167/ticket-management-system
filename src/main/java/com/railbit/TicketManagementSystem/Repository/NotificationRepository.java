package com.railbit.TicketManagementSystem.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.railbit.TicketManagementSystem.Entity.Notification;
import com.railbit.TicketManagementSystem.Entity.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientAndIsReadFalse(User recipient);
    
    
    @Query("SELECT u FROM User u WHERE u.role = 'ROLE_ADMIN'")
    User findAdminUser();

}