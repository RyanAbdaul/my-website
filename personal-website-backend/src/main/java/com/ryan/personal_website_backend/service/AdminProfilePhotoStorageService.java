package com.ryan.personal_website_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminProfilePhotoStorageService {
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Map<String, String> EXTENSIONS = Map.of(
            MediaType.IMAGE_JPEG_VALUE, "jpg",
            MediaType.IMAGE_PNG_VALUE, "png",
            "image/webp", "webp"
    );

    private final Path photosDirectory;

    public AdminProfilePhotoStorageService(@Value("${app.upload-dir}") String uploadDir) {
        photosDirectory = Path.of(uploadDir).toAbsolutePath().normalize().resolve("admin-profile");
    }

    public String store(MultipartFile photo) {
        if (photo == null || photo.isEmpty()) throw new IllegalArgumentException("Please choose an image file.");
        if (photo.getSize() > MAX_FILE_SIZE) throw new IllegalArgumentException("Image files must be 5 MB or smaller.");

        String extension = EXTENSIONS.get(photo.getContentType());
        if (extension == null) throw new IllegalArgumentException("Use a JPG, PNG, or WebP image.");

        try {
            Files.createDirectories(photosDirectory);
            String filename = UUID.randomUUID() + "." + extension;
            Files.copy(photo.getInputStream(), photosDirectory.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return "/api/uploads/admin-profile/" + filename;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save the image.", exception);
        }
    }
}
