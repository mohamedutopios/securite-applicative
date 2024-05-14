package com.example.bruteforce.security;

import com.example.bruteforce.model.LoginAttempt;
import com.example.bruteforce.model.User;
import com.example.bruteforce.repository.LoginAttemptRepository;
import com.example.bruteforce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCK_TIME_DURATION = 1;

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    @Autowired
    private UserRepository userRepository;

    public void loginSucceeded(String username) {
        loginAttemptRepository.deleteAll(loginAttemptRepository.findByUsernameAndTimestampAfter(username, LocalDateTime.now().minusMinutes(LOCK_TIME_DURATION)));
    }

    public void loginFailed(String username) {
        LocalDateTime now = LocalDateTime.now();
        loginAttemptRepository.save(new LoginAttempt(username, now));

        if (isBruteForceAttack(username)) {
            User user = userRepository.findByUsername(username).orElseThrow();
            user.setAccountNonLocked(false);
            userRepository.save(user);
        }
    }

    private boolean isBruteForceAttack(String username) {
        LocalDateTime lockTime = LocalDateTime.now().minusMinutes(LOCK_TIME_DURATION);
        return loginAttemptRepository.findByUsernameAndTimestampAfter(username, lockTime).size() >= MAX_ATTEMPTS;
    }

    // Méthode pour récupérer le temps de verrouillage
    public LocalDateTime getLockTime(String username) {
        LoginAttempt attempt = loginAttemptRepository.findTopByUsernameOrderByTimestampDesc(username);
        return attempt != null ? attempt.getTimestamp() : null;
    }

    // Méthode pour déverrouiller le compte
    public void unlockAccount(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setAccountNonLocked(true);
        userRepository.save(user);
    }
}
