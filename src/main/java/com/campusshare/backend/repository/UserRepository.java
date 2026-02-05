package com.campusshare.backend.repository;

import com.campusshare.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    //cari user berdasarkan email untuk Login
    Optional<User> findByEmail(String email);
}