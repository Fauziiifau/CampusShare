package com.campusshare.backend.controller;

import com.campusshare.backend.entity.User;
import com.campusshare.backend.repository.UserRepository;
import com.campusshare.backend.entity.Note;
import com.campusshare.backend.repository.NoteRepository;
import com.campusshare.backend.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notes")

public class NoteController {
    // Constructor injection is explicit to avoid Lombok-related compilation issues.
    public NoteController(FileStorageService fileStorageService, NoteRepository noteRepository, UserRepository userRepository) {
        this.fileStorageService = fileStorageService;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }


    private final FileStorageService fileStorageService;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> uploadNote(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("subject") String subject,
            @RequestParam("campus") String campus,
            Principal principal) { // Principal = Data user dari Token

        String emailUser = principal.getName();
        String fileName = fileStorageService.storeFile(file);
        Note note = new Note();
        note.setTitle(title);
        note.setDescription(description);
        note.setSubject(subject);
        note.setCampus(campus);
        note.setFileUrl(fileName);
        note.setUploaderEmail(emailUser);
        noteRepository.save(note);

        return ResponseEntity.ok("Berhasil upload oleh user: " + emailUser);
    }
    @GetMapping
    public List<Note> getAllNotes(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return noteRepository.findByTitleContainingIgnoreCaseOrSubjectContainingIgnoreCase(keyword, keyword);
        } else {
            return noteRepository.findAll();
        }
    }
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
         Resource resource = fileStorageService.loadFileAsResource(fileName);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable Long id, Principal principal) {
        String emailUser = principal.getName();

        // 1. Cari data user yang sedang request ini (buat ngecek role-nya)
        User currentUser = userRepository.findByEmail(emailUser)
                .orElseThrow(() -> new RuntimeException("User tidak valid"));

        // 2. Cari catatan
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catatan tidak ditemukan"));

        // 3. LOGIKA BARU: Cek Hak Akses
        // Boleh hapus jika: Dia PEMILIKNYA --ATAU-- Dia ADMIN
        boolean isOwner = note.getUploaderEmail().equals(emailUser);
        boolean isAdmin = "ADMIN".equals(currentUser.getRole()); // Cek string "ADMIN"

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(403).body("Anda tidak berhak menghapus catatan ini!");
        }

        // 4. Hapus File Fisik
        try {
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get("uploads").resolve(note.getFileUrl()));
        } catch (IOException e) { }

        // 5. Hapus Data DB
        noteRepository.delete(note);

        return ResponseEntity.ok("Catatan berhasil dihapus.");
    }
}