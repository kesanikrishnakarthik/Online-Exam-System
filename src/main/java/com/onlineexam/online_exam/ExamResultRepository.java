package com.onlineexam.online_exam;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {

    List<ExamResult> findByCollegeCode(String collegeCode);

    List<ExamResult> findByStudentUsername(String studentUsername);

    List<ExamResult> findByCollegeCodeAndSubject(String collegeCode, String subject);

    List<ExamResult> findByExamId(Long examId);
}
