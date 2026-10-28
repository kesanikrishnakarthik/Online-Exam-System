package com.onlineexam.online_exam;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollegeRepository extends JpaRepository<College, Long> {

    Optional<College> findByCode(String code);

    Optional<College> findByCodeIgnoreCase(String code);

    List<College> findByStatus(String status);

    List<College> findByStatusIgnoreCase(String status);

    List<College> findByStatusIn(Collection<String> statuses);

    boolean existsByCode(String code);

    boolean existsByCodeIgnoreCase(String code);
}


