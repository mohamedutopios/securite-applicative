package com.example.bruteforce.repository;

import com.example.bruteforce.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    List<LoginAttempt> findByUsernameAndTimestampAfter(String username, LocalDateTime timestamp);
}
