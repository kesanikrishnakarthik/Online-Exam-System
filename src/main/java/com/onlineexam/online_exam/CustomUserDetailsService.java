package com.onlineexam.online_exam;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public CustomUserDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String loginIdentifier) throws UsernameNotFoundException {
        if (loginIdentifier == null || loginIdentifier.trim().isEmpty()) {
            throw new UsernameNotFoundException("Login identifier cannot be empty");
        }

        String trimmed = loginIdentifier.trim();
        Optional<UserAccount> userOpt = userAccountRepository.findByUsername(trimmed);

        // 1. Check if identifier contains college prefix e.g. "AIT:21B91A0501" or "AIT/21B91A0501"
        if (userOpt.isEmpty() && (trimmed.contains(":") || trimmed.contains("/"))) {
            String[] parts = trimmed.split("[:/]", 2);
            userOpt = userAccountRepository.findByCollegeCodeAndInstitutionalId(
                    parts[0].trim().toUpperCase(), parts[1].trim());
        }

        // 2. Check if identifier directly matches student Roll Number or Faculty ID
        if (userOpt.isEmpty()) {
            List<UserAccount> matchingById = userAccountRepository.findAllByInstitutionalIdIgnoreCase(trimmed);
            if (matchingById.size() == 1) {
                userOpt = Optional.of(matchingById.get(0));
            } else if (matchingById.size() > 1) {
                // Multiple colleges share the same roll number; prompt user to specify college code prefix
                throw new UsernameNotFoundException(
                        "Multiple accounts found for '" + trimmed + "'. Please log in as COLLEGE_CODE:" + trimmed + " (e.g. AIT:" + trimmed + ")");
            }
        }

        // 3. Fallback: check if login identifier is registered institutional email
        if (userOpt.isEmpty()) {
            userOpt = userAccountRepository.findByEmailIgnoreCase(trimmed);
        }

        UserAccount account = userOpt.orElseThrow(() ->
                new UsernameNotFoundException("No active institutional account found for: " + trimmed));

        if ("PENDING".equalsIgnoreCase(account.getStatus()) || "PENDING_APPROVAL".equalsIgnoreCase(account.getStatus())) {
            throw new DisabledException("Your account is pending administrative approval.");
        }

        if ("REJECTED".equalsIgnoreCase(account.getStatus())) {
            throw new DisabledException("Your account registration application was not approved by administration.");
        }

        if ("SUSPENDED".equalsIgnoreCase(account.getStatus())) {
            throw new LockedException("Your institutional account has been suspended by administration.");
        }

        String role = account.getRole();
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        return new User(
                account.getUsername(),
                account.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}
