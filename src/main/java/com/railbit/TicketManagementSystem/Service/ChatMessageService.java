package com.railbit.TicketManagementSystem.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.railbit.TicketManagementSystem.Entity.ChatMessage;
import com.railbit.TicketManagementSystem.Entity.User;
import com.railbit.TicketManagementSystem.Repository.ChatMessageRepository;

//package: com.railbit.TicketManagementSystem.Service
@Service
public class ChatMessageService {

 @Autowired
 private ChatMessageRepository chatRepo;

 public void sendMessage(User sender, User receiver, String message) {
     ChatMessage chat = new ChatMessage();
     chat.setSender(sender);
     chat.setReceiver(receiver);
     chat.setMessage(message);
     chatRepo.save(chat);
 }

 public List<ChatMessage> getChat(Long user1Id, Long user2Id) {
	    return chatRepo.getConversation(user1Id, user2Id);
	}

}
