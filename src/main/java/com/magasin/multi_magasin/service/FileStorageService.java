package com.magasin.multi_magasin.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadRoot = Paths.get("uploads");

    public String storeImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String ext = "";
        int lastDot = originalFilename.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < originalFilename.length() - 1) {
            ext = originalFilename.substring(lastDot).toLowerCase();
        }

        String filename = UUID.randomUUID() + ext;

        Files.createDirectories(uploadRoot);
        Path target = uploadRoot.resolve(filename).normalize();
        Files.copy(file.getInputStream(), target);

        return "/uploads/" + filename;
    }
}
