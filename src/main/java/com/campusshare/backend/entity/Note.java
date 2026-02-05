package com.campusshare.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Data
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
}