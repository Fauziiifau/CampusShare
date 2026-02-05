package com.campusshare.backend.repository;

import com.campusshare.backend.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByTitleContainingIgnoreCaseOrSubjectContainingIgnoreCase(String title, String subject);
}