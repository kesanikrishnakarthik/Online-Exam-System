package com.onlineexam.online_exam;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepository,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static String hashToken(String rawToken) {
        if (rawToken == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    @Transactional
    public String createResetToken(String username, String email) {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        String rawToken = HexFormat.of().formatHex(randomBytes);

        String tokenHash = hashToken(rawToken);
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(30);

        // Invalidate older unused tokens for this user
        List<PasswordResetToken> existingTokens = tokenRepository.findByUsername(username);
        for (PasswordResetToken t : existingTokens) {
            if (!t.isUsed()) {
                t.setUsed(true);
                tokenRepository.save(t);
            }
        }

        PasswordResetToken token = new PasswordResetToken(tokenHash, username, email, expiryDate);
        tokenRepository.save(token);

        return rawToken;
    }

    public Optional<PasswordResetToken> validateToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }

        String tokenHash = hashToken(rawToken.trim());
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByTokenHashAndUsedFalse(tokenHash);

        if (tokenOpt.isPresent()) {
            PasswordResetToken token = tokenOpt.get();
            if (!token.isExpired()) {
                return Optional.of(token);
            }
        }

        return Optional.empty();
    }

    @Transactional
    public boolean resetPassword(String rawToken, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = validateToken(rawToken);
        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken token = tokenOpt.get();
        Optional<UserAccount> userOpt = userAccountRepository.findByUsername(token.getUsername());
        if (userOpt.isEmpty()) {
            return false;
        }

        UserAccount user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userAccountRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);

        return true;
    }
}
