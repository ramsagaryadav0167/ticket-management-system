package com.railbit.TicketManagementSystem.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class StorageServiceImpl implements StorageService {

    private final Path uploadDir = Paths.get("uploads");

    public StorageServiceImpl() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            return null;
        }

        try {
            String filename = Path.of(file.getOriginalFilename()).getFileName().toString(); // Prevent path traversal
            Path destination = uploadDir.resolve(filename).normalize();
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
}
