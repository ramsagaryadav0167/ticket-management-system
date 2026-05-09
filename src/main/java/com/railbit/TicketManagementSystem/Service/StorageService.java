package com.railbit.TicketManagementSystem.Service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
	

	String storeFile(MultipartFile file);
}
