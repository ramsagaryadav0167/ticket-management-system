package com.railbit.TicketManagementSystem.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.railbit.TicketManagementSystem.Entity.ServicesRequest;
import com.railbit.TicketManagementSystem.Repository.ServicesRequestRepository;

import java.util.List;

@Service // ✅ Marks this class as a Spring Service component (used for business logic)
public class ServicesService {

    @Autowired // ✅ Injects the ServicesRequestRepository bean into this service
    private ServicesRequestRepository servicesRequestRepository;

    /**
     * ✅ Save a service request to the database
     * @param request the ServicesRequest entity object to be persisted
     */
    public void saveServiceRequest(ServicesRequest request) {
        servicesRequestRepository.save(request); // 🔹 Uses JPA repository's save() to insert/update data
    }

    /**
     * ✅ Retrieve all service requests from the database
     * @return a list of ServicesRequest objects
     */
    public List<ServicesRequest> getAllServiceRequests() {
        return servicesRequestRepository.findAll(); // 🔹 Uses findAll() method to get all entries
    }
    public void deleteById(Long id) {
        servicesRequestRepository.deleteById(id);
    }

}
