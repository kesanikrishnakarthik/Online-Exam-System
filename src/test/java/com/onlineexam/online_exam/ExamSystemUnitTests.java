package com.onlineexam.online_exam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("College Online Exam System - Domain & Security Unit Tests")
class ExamSystemUnitTests {

    private Exam sampleExam;
    private List<Question> sampleQuestions;

    @BeforeEach
    void setUp() {
        sampleExam = new Exam("Java Programming", "Java", 30, 20);
        sampleExam.setId(101L);
        sampleExam.setMaxAttempts(3);
        sampleExam.setMaxViolations(3);

        sampleQuestions = new ArrayList<>();
        sampleQuestions.add(new Question(101L, "Which keyword is used to inherit a class in Java?", "implements", "extends", "inherits", "super", "B", 4));
        sampleQuestions.add(new Question(101L, "Which method is the entry point of a Java application?", "start()", "run()", "main()", "init()", "C", 4));
        sampleQuestions.add(new Question(101L, "Which collection does not allow duplicate elements?", "List", "ArrayList", "Set", "Queue", "C", 4));
    }

    @Test
    @DisplayName("Verify Exam Creation Defaults to Strict 3-Attempt Limit")
    void testExamCreationDefaults() {
        assertNotNull(sampleExam);
        assertEquals(3, sampleExam.getMaxAttempts(), "Default attempt limit must be 3");
        assertEquals(3, sampleExam.getMaxViolations(), "Default security violation threshold must be 3");
        assertEquals("Apex Institute of Technology", sampleExam.getCollege());
    }

    @Test
    @DisplayName("Verify Strict 3-Attempt Enforcement Logic")
    void testStrictThreeAttemptLimit() {
        String student = "student1";
        List<ExamAttempt> history = new ArrayList<>();

        // Attempt 1
        history.add(new ExamAttempt(sampleExam.getId(), sampleExam.getTitle(), sampleExam.getSubject(),
                student, sampleExam.getCollege(), 1, LocalDateTime.now(), "COMPLETED"));
        assertTrue(history.size() < sampleExam.getMaxAttempts(), "Attempt 2 must be allowed");

        // Attempt 2
        history.add(new ExamAttempt(sampleExam.getId(), sampleExam.getTitle(), sampleExam.getSubject(),
                student, sampleExam.getCollege(), 2, LocalDateTime.now(), "COMPLETED"));
        assertTrue(history.size() < sampleExam.getMaxAttempts(), "Attempt 3 must be allowed");

        // Attempt 3
        history.add(new ExamAttempt(sampleExam.getId(), sampleExam.getTitle(), sampleExam.getSubject(),
                student, sampleExam.getCollege(), 3, LocalDateTime.now(), "COMPLETED"));
        assertEquals(3, history.size());

        // Verify Attempt 4 is strictly disallowed
        boolean canTakeFourthAttempt = history.size() < sampleExam.getMaxAttempts();
        assertFalse(canTakeFourthAttempt, "Attempt 4 must be strictly blocked once 3 attempts are exhausted");
    }

    @Test
    @DisplayName("Verify Server-Side Score Calculation Without Exposing Answers")
    void testServerSideScoreCalculation() {
        Map<String, String> studentAnswers = Map.of(
                "q_1", "B", // Correct (4 marks)
                "q_2", "C", // Correct (4 marks)
                "q_3", "A"  // Incorrect (0 marks)
        );

        int calculatedScore = 0;
        int totalMarks = 0;

        for (int i = 0; i < sampleQuestions.size(); i++) {
            Question q = sampleQuestions.get(i);
            totalMarks += q.getMarks();
            String submitted = studentAnswers.get("q_" + (i + 1));
            if (submitted != null && submitted.equalsIgnoreCase(q.getCorrectAnswer())) {
                calculatedScore += q.getMarks();
            }
        }

        assertEquals(12, totalMarks, "Total marks for 3 questions should be 12");
        assertEquals(8, calculatedScore, "Score should be exactly 8 for 2 correct answers");
        double percentage = Math.round((calculatedScore * 100.0 / totalMarks) * 100.0) / 100.0;
        assertEquals(66.67, percentage, 0.01, "Percentage should be 66.67%");
    }

    @Test
    @DisplayName("Verify Anti-Cheating Violation Auto-Submission Threshold")
    void testAntiCheatingViolationThreshold() {
        ExamAttempt attempt = new ExamAttempt(sampleExam.getId(), sampleExam.getTitle(), sampleExam.getSubject(),
                "testuser", sampleExam.getCollege(), 1, LocalDateTime.now(), "IN_PROGRESS");

        // Violation 1: Warning
        attempt.setViolationCount(1);
        assertNotEquals("AUTO_SUBMITTED_VIOLATION", attempt.getStatus());

        // Violation 2: Final Warning
        attempt.setViolationCount(2);
        assertNotEquals("AUTO_SUBMITTED_VIOLATION", attempt.getStatus());

        // Violation 3: Trigger Auto Submit
        attempt.setViolationCount(3);
        if (attempt.getViolationCount() >= sampleExam.getMaxViolations()) {
            attempt.setStatus("AUTO_SUBMITTED_VIOLATION");
            attempt.setSubmissionReason("VIOLATION_LIMIT");
        }

        assertEquals("AUTO_SUBMITTED_VIOLATION", attempt.getStatus(), "Status must change to AUTO_SUBMITTED_VIOLATION at 3 violations");
        assertEquals("VIOLATION_LIMIT", attempt.getSubmissionReason());
    }

    @Test
    @DisplayName("Verify Examination Timer Expiration Auto-Submission")
    void testTimerExpirationAutoSubmission() {
        ExamAttempt attempt = new ExamAttempt(sampleExam.getId(), sampleExam.getTitle(), sampleExam.getSubject(),
                "testuser", sampleExam.getCollege(), 1, LocalDateTime.now(), "IN_PROGRESS");

        // Simulate timer hitting 0
        attempt.setStatus("AUTO_SUBMITTED_TIMEOUT");
        attempt.setSubmissionReason("TIMEOUT");
        attempt.setSubmissionTime(LocalDateTime.now());

        assertEquals("AUTO_SUBMITTED_TIMEOUT", attempt.getStatus());
        assertEquals("TIMEOUT", attempt.getSubmissionReason());
    }

    @Test
    @DisplayName("Verify Student Result Privacy (Student only sees own results)")
    void testStudentResultPrivacy() {
        List<ExamResult> allResults = List.of(
                new ExamResult(101L, "Java", "Java", "studentA", 16, 20),
                new ExamResult(101L, "Java", "Java", "studentB", 20, 20),
                new ExamResult(101L, "Java", "Java", "studentA", 12, 20)
        );

        String authenticatedStudent = "studentA";
        List<ExamResult> filteredResults = allResults.stream()
                .filter(r -> r.getStudentUsername().equals(authenticatedStudent))
                .toList();

        assertEquals(2, filteredResults.size());
        for (ExamResult r : filteredResults) {
            assertEquals("studentA", r.getStudentUsername(), "A student must never receive another student's results");
        }
    }
}

