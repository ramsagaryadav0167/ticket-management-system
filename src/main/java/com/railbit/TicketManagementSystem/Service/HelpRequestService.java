package com.railbit.TicketManagementSystem.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.railbit.TicketManagementSystem.Entity.HelpRequest;
import com.railbit.TicketManagementSystem.Repository.HelpRequestRepository;

@Service // ✅ Marks this class as a Spring service (business logic layer)
public class HelpRequestService {

    @Autowired // ✅ Automatically injects the HelpRequestRepository instance
    private HelpRequestRepository helpRepo;

    // ✅ Saves a HelpRequest object to the database
    public void saveRequest(HelpRequest helpRequest) {
        helpRepo.save(helpRequest); // 🔹 Delegates the saving to JPA repository
    }
}
