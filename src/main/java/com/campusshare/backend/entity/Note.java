package com.campusshare.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String subject; // Mata Kuliah
    private String campus;  // Nama Kampus
    private String fileUrl;
    private String uploaderEmail;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Explicit JavaBean accessors: no Lombok annotation processing needed.
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return this.title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }
    public String getSubject() { return this.subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getCampus() { return this.campus; }
    public void setCampus(String campus) { this.campus = campus; }
    public String getFileUrl() { return this.fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getUploaderEmail() { return this.uploaderEmail; }
    public void setUploaderEmail(String uploaderEmail) { this.uploaderEmail = uploaderEmail; }
    public LocalDateTime getCreatedAt() { return this.createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}