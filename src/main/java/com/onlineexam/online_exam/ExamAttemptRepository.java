package com.onlineexam.online_exam;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {

    List<ExamAttempt> findByExamIdAndStudentUsernameOrderByAttemptNumberDesc(Long examId, String studentUsername);

    List<ExamAttempt> findByExamIdAndStudentUsername(Long examId, String studentUsername);

    long countByExamIdAndStudentUsername(Long examId, String studentUsername);

    List<ExamAttempt> findByStudentUsernameOrderBySubmissionTimeDesc(String studentUsername);

    List<ExamAttempt> findByExamId(Long examId);

    Optional<ExamAttempt> findFirstByExamIdAndStudentUsernameAndStatus(Long examId, String studentUsername, String status);
}

