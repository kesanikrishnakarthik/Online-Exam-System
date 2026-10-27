package com.onlineexam.online_exam;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findByCollegeCodeAndRollNumberOrFacultyId(String collegeCode, String rollNumberOrFacultyId);

    List<UserAccount> findByCollegeCodeAndRole(String collegeCode, String role);

    List<UserAccount> findByCollegeCode(String collegeCode);

    List<UserAccount> findByStatus(String status);

    boolean existsByUsername(String username);

    boolean existsByCollegeCodeAndRollNumberOrFacultyId(String collegeCode, String rollNumberOrFacultyId);
}

