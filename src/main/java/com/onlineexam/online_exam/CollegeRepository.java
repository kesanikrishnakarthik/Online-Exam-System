package com.onlineexam.online_exam;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollegeRepository extends JpaRepository<College, Long> {

    Optional<College> findByCode(String code);

    List<College> findByStatus(String status);

    boolean existsByCode(String code);
}

