package com.railbit.TicketManagementSystem.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.railbit.TicketManagementSystem.Entity.ChatMessage;
import com.railbit.TicketManagementSystem.Entity.User;
import com.railbit.TicketManagementSystem.Service.ChatMessageService;
import com.railbit.TicketManagementSystem.Service.UserService;

//package: com.railbit.TicketManagementSystem.Controller
@RestController
@RequestMapping("/admin/chat")
public class AdminChatController {

    @Autowired
    private ChatMessageService chatService;

    @Autowired
    private UserService userService;

    @PostMapping("/send")
    public ResponseEntity<Void> sendMessage(@RequestParam Long toId, @RequestParam String message, Principal principal) {
        User sender = userService.findByUsername(principal.getName()); // ✅ FIXED
        User receiver = userService.getUserById(toId);                 // ✅ FIXED
        chatService.sendMessage(sender, receiver, message);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/messages/{userId}")
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable Long userId, Principal principal) {
        User me = userService.findByUsername(principal.getName()); // ✅ FIXED
        return ResponseEntity.ok(chatService.getChat(me.getId(), userId));
    }
    
    @GetMapping("/users")
    public ResponseEntity<List<User>> getChatUsers() {
        List<User> users = userService.getAllAgentsAndCustomers(); // ✅ both agents and customers
        return ResponseEntity.ok(users);
    }


}

