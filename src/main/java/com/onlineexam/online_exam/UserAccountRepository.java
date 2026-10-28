package com.onlineexam.online_exam;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findByCollegeCodeAndInstitutionalId(String collegeCode, String institutionalId);

    List<UserAccount> findAllByInstitutionalIdIgnoreCase(String institutionalId);

    Optional<UserAccount> findByEmailIgnoreCase(String email);

    List<UserAccount> findByCollegeCodeAndRole(String collegeCode, String role);

    List<UserAccount> findByCollegeCode(String collegeCode);

    List<UserAccount> findByRole(String role);

    List<UserAccount> findByStatus(String status);

    boolean existsByUsername(String username);

    boolean existsByCollegeCodeAndInstitutionalId(String collegeCode, String institutionalId);

    boolean existsByEmailIgnoreCase(String email);
}
