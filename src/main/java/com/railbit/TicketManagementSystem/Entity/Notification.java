package com.railbit.TicketManagementSystem.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Notification {
@Id
@GeneratedValue(strategy=GenerationType.IDENTITY)
private  Long id;

private String message;

private boolean isRead=false;

private LocalDateTime timestamp;

@ManyToOne
@JoinColumn(name="recipient_id")
private User recipient;

// Constructors
public Notification() {}

public Notification(String message, User recipient) {
    this.message = message;
    this.recipient = recipient;
    this.timestamp = LocalDateTime.now();
    this.isRead = false;
}

public Long getId() {
	return id;
}

public void setId(Long id) {
	this.id = id;
}

public String getMessage() {
	return message;
}

public void setMessage(String message) {
	this.message = message;
}

public boolean isRead() {
	return isRead;
}

public void setRead(boolean isRead) {
	this.isRead = isRead;
}

public LocalDateTime getTimestamp() {
	return timestamp;
}

public void setTimestamp(LocalDateTime timestamp) {
	this.timestamp = timestamp;
}

public User getRecipient() {
	return recipient;
}

public void setRecipient(User recipient) {
	this.recipient = recipient;
}


}
