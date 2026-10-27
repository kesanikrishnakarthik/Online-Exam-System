package com.onlineexam.online_exam;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByCollegeCode(String collegeCode);

    List<Exam> findByCollegeCodeAndLifecycleStatus(String collegeCode, String lifecycleStatus);
}
