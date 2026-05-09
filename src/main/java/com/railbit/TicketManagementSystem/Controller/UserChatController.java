package com.railbit.TicketManagementSystem.Controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.railbit.TicketManagementSystem.Entity.ChatMessage;
import com.railbit.TicketManagementSystem.Entity.User;
import com.railbit.TicketManagementSystem.Service.ChatMessageService;
import com.railbit.TicketManagementSystem.Service.UserService;

@RestController
@RequestMapping("/user/chat")
public class UserChatController {

    @Autowired
    private ChatMessageService chatService;

    @Autowired
    private UserService userService;

    // ✅ 1. Send message from user/customer to admin
    @PostMapping("/send")
    public ResponseEntity<Void> sendMessage(@RequestParam Long toId, @RequestParam String message, Principal principal) {
        User sender = userService.findByUsername(principal.getName());
        User receiver = userService.getUserById(toId);
        chatService.sendMessage(sender, receiver, message);
        return ResponseEntity.ok().build();
    }

    // ✅ 2. Fetch all messages between logged-in user and admin
    @GetMapping("/messages/{receiverId}")
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable Long receiverId, Principal principal) {
        User me = userService.findByUsername(principal.getName());
        return ResponseEntity.ok(chatService.getChat(me.getId(), receiverId));
    }

    // ✅ 3. Used in frontend JavaScript to load adminId automatically
    @GetMapping("/admin-id")
    public ResponseEntity<Long> getAdminId() {
        return ResponseEntity.ok(userService.getAdminUser().getId());
    }

    // ✅ 4. Optional: Get all customers (if needed in user-side dropdown)
    @GetMapping("/customers")
    public ResponseEntity<List<User>> getCustomers() {
        return ResponseEntity.ok(userService.getUsersByRole("ROLE_CUSTOMER"));
    }
 // 👇 UserChatController.java me ye method add karo
    @GetMapping("/admin")
    public ResponseEntity<List<User>> getAdminUser() {
        return ResponseEntity.ok(userService.getUsersByRole("ROLE_ADMIN"));
    }

}
