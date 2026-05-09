package com.railbit.TicketManagementSystem.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.railbit.TicketManagementSystem.Entity.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByToken(String token);
}

