package com.example.DATN.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileUploadService {

    private final Path rootPath = Paths.get("uploads", "products");

    public FileUploadService() {
        try {
            if (!Files.exists(rootPath)) {
                Files.createDirectories(rootPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload folder!", e);
        }
    }

    public String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String cleanName = UUID.randomUUID().toString().substring(0, 12) + extension;

            // Save to root uploads/products/
            Path destination = rootPath.resolve(cleanName);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            // Also copy to target/classes/static/uploads/products if exists for dev hot-reload
            Path targetPath = Paths.get("target", "classes", "static", "uploads", "products");
            if (Files.exists(targetPath)) {
                try {
                    Files.copy(destination, targetPath.resolve(cleanName), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception ignored) {}
            }

            return "/uploads/products/" + cleanName;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi lưu file tải lên: " + e.getMessage(), e);
        }
    }
}
