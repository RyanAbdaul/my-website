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
public class MemberPhotoStorageService {
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Map<String, String> EXTENSIONS = Map.of(
            MediaType.IMAGE_JPEG_VALUE, "jpg",
            MediaType.IMAGE_PNG_VALUE, "png",
            "image/webp", "webp"
    );

    private final Path memberPhotosDirectory;

    public MemberPhotoStorageService(@Value("${app.upload-dir}") String uploadDir) {
        this.memberPhotosDirectory = Path.of(uploadDir).toAbsolutePath().normalize().resolve("member-photos");
    }

    public String store(MultipartFile photo) {
        if (photo == null || photo.isEmpty()) throw new IllegalArgumentException("Please choose an image file.");
        if (photo.getSize() > MAX_FILE_SIZE) throw new IllegalArgumentException("Image files must be 5 MB or smaller.");

        String contentType = photo.getContentType();
        String extension = EXTENSIONS.get(contentType);
        if (extension == null) throw new IllegalArgumentException("Use a JPG, PNG, or WebP image.");

        try {
            Files.createDirectories(memberPhotosDirectory);
            String filename = UUID.randomUUID() + "." + extension;
            Files.copy(photo.getInputStream(), memberPhotosDirectory.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return "/api/uploads/member-photos/" + filename;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save the image.", exception);
        }
    }
}
