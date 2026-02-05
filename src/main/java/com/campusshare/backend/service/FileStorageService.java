package com.campusshare.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class FileStorageService {
    private final Path rootLocation = Paths.get("uploads");

    public FileStorageService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Gagal bikin folder penyimpanan!", e);
        }
    }

    public String storeFile(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Gagal: File kosong.");
            }
            String originalFilename = file.getOriginalFilename();
            String filename = System.currentTimeMillis() + "_" + originalFilename;
            Files.copy(file.getInputStream(),
                    this.rootLocation.resolve(filename),
                    StandardCopyOption.REPLACE_EXISTING);
            return filename;

        } catch (IOException e) {
            throw new RuntimeException("Gagal menyimpan file!", e);
        }
    }
    public org.springframework.core.io.Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.rootLocation.resolve(fileName).normalize();
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File tidak ditemukan: " + fileName);
            }
        } catch (java.net.MalformedURLException e) {
            throw new RuntimeException("File tidak ditemukan (Error URL)", e);
        }
    }
}