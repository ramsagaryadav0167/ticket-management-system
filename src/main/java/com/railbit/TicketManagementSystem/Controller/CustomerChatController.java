package com.railbit.TicketManagementSystem.Controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.railbit.TicketManagementSystem.Entity.ChatMessage;
import com.railbit.TicketManagementSystem.Entity.Ticket;
import com.railbit.TicketManagementSystem.Entity.User;
import com.railbit.TicketManagementSystem.Service.ChatMessageService;
import com.railbit.TicketManagementSystem.Service.TicketService;
import com.railbit.TicketManagementSystem.Service.UserService;

@RestController
@RequestMapping("/customer/chat")
public class CustomerChatController {

    @Autowired
    private ChatMessageService chatService;

    @Autowired
    private UserService userService;

    @Autowired
    private TicketService ticketService;

    // ✅ Get assigned agent from the latest ticket or fallback to admin
    private User getAssignedAgentForCustomer(User customer) {
        return ticketService.getLatestTicketByCustomer(customer)
                .map(Ticket::getAssignedTo)
                .orElse(userService.getAdminUser()); // ✅ FIXED
    }


    // ✅ Send message to assigned agent
    @PostMapping("/send")
    public ResponseEntity<Void> sendMessage(@RequestParam String message, Principal principal) {
        User sender = userService.findByUsername(principal.getName());
        User receiver = getAssignedAgentForCustomer(sender);
        chatService.sendMessage(sender, receiver, message);
        return ResponseEntity.ok().build();
    }

    // ✅ Fetch chat history
    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessage>> getMessages(Principal principal) {
        User sender = userService.findByUsername(principal.getName());
        User receiver = getAssignedAgentForCustomer(sender);
        return ResponseEntity.ok(chatService.getChat(sender.getId(), receiver.getId()));
    }

    // ✅ Get assigned agent info (for UI)
 // ✅ Get all distinct assigned agents for this customer
    @GetMapping("/agents")
    public ResponseEntity<List<User>> getAllAssignedAgents(Principal principal) {
        User customer = userService.findByUsername(principal.getName());
        List<User> agents = ticketService.getAllAssignedAgentsByCustomer(customer);
        return ResponseEntity.ok(agents);
    }

}
