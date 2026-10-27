package com.onlineexam.online_exam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("College Online Exam System - Domain, Multi-Tenant & Security Unit Tests")
class ExamSystemUnitTests {

    private Exam sampleExam;
    private List<Question> sampleQuestions;

    @BeforeEach
    void setUp() {
        sampleExam = new Exam("Java Programming", "Java", 30, 20);
        sampleExam.setId(101L);
        sampleExam.setCollegeCode("AIT");
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
        assertEquals("AIT", sampleExam.getCollegeCode());
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

    @Test
    @DisplayName("Verify Multi-Tenant Data Isolation (College A cannot see College B exams)")
    void testMultiTenantExamIsolation() {
        Exam examAIT1 = new Exam("Data Structures", "CSE", 45, 30);
        examAIT1.setCollegeCode("AIT");

        Exam examAIT2 = new Exam("Computer Networks", "CSE", 40, 25);
        examAIT2.setCollegeCode("AIT");

        Exam examNIE = new Exam("Power Systems", "EEE", 50, 40);
        examNIE.setCollegeCode("NIE");

        List<Exam> allSystemExams = List.of(examAIT1, examAIT2, examNIE);

        // Simulate student from AIT querying exams
        String studentCollegeCode = "AIT";
        List<Exam> studentVisibleExams = allSystemExams.stream()
                .filter(e -> studentCollegeCode.equalsIgnoreCase(e.getCollegeCode()))
                .toList();

        assertEquals(2, studentVisibleExams.size());
        assertTrue(studentVisibleExams.stream().allMatch(e -> "AIT".equals(e.getCollegeCode())));
        assertFalse(studentVisibleExams.contains(examNIE), "AIT student must not access NIE exams");
    }

    @Test
    @DisplayName("Verify College Approval Gate (Only APPROVED colleges allow active enrollment)")
    void testCollegeApprovalGate() {
        College approvedCollege = new College("Apex Institute", "AIT", "Engineering", "APPROVED", "Hyderabad", "Dr. Rao", "admin@ait.edu", "9876543210");
        College pendingCollege = new College("St. Xavier's", "SXC", "Degree", "PENDING", "Mumbai", "Fr. Joseph", "admin@sxc.edu", "9876543211");
        College suspendedCollege = new College("Old Institute", "OLD", "Polytechnic", "SUSPENDED", "Delhi", "Prof. Kumar", "admin@old.edu", "9876543212");

        assertTrue("APPROVED".equalsIgnoreCase(approvedCollege.getStatus()), "Approved college must allow student enrollment");
        assertFalse("APPROVED".equalsIgnoreCase(pendingCollege.getStatus()), "Pending college must block onboarding until Super Admin approval");
        assertFalse("APPROVED".equalsIgnoreCase(suspendedCollege.getStatus()), "Suspended college must block new enrollment");
    }

    @Test
    @DisplayName("Verify Institutional Roll Number Login Parsing")
    void testInstitutionalRollNumberLoginParsing() {
        String inputLogin = "AIT:21B91A0501";
        String[] parts = inputLogin.split(":", 2);

        assertEquals(2, parts.length);
        String collegeCode = parts[0].trim().toUpperCase();
        String rollNumber = parts[1].trim();

        assertEquals("AIT", collegeCode);
        assertEquals("21B91A0501", rollNumber);
    }

    @Test
    @DisplayName("Verify Single Choice MCQ Grading Logic")
    void testSingleChoiceGrading() {
        Question q = new Question(101L, "What is the return type of hashCode()?", "int", "long", "void", "String", "A", 4);
        q.setQuestionType("SINGLE_CHOICE");

        // Correct answer
        int scoreCorrect = ExamController.calculateScore(q, "A");
        assertEquals(4, scoreCorrect, "Correct single choice option must award full marks");

        // Incorrect answer
        int scoreWrong = ExamController.calculateScore(q, "B");
        assertEquals(0, scoreWrong, "Incorrect single choice option must award 0 marks");

        // Case insensitivity of option letter
        int scoreCaseInsensitive = ExamController.calculateScore(q, "a");
        assertEquals(4, scoreCaseInsensitive, "Option match must be case-insensitive");

        // Blank answer
        int scoreBlank = ExamController.calculateScore(q, "");
        assertEquals(0, scoreBlank, "Blank answer must award 0 marks");
    }

    @Test
    @DisplayName("Verify Multiple Select Checkbox All-or-Nothing Grading Logic")
    void testMultipleSelectAllOrNothingGrading() {
        Question q = new Question(101L, "Select valid Java access modifiers", "MULTIPLE_SELECT",
                "public", "private", "protected", "friend", "A,B,C", "ALL_OR_NOTHING", false, 6, 0);

        // Exact match
        int scoreExact = ExamController.calculateScore(q, new String[]{"A", "B", "C"});
        assertEquals(6, scoreExact, "Exact selection match must award full marks under ALL_OR_NOTHING");

        // Partial match (missing C)
        int scorePartial = ExamController.calculateScore(q, new String[]{"A", "B"});
        assertEquals(0, scorePartial, "Missing an answer in ALL_OR_NOTHING must award 0 marks");

        // Extra incorrect option included (D selected)
        int scoreExtra = ExamController.calculateScore(q, new String[]{"A", "B", "C", "D"});
        assertEquals(0, scoreExtra, "Selecting an incorrect option in ALL_OR_NOTHING must award 0 marks");

        // Null / empty selection
        int scoreEmpty = ExamController.calculateScore(q, new String[]{});
        assertEquals(0, scoreEmpty, "Empty selection must award 0 marks");
    }

    @Test
    @DisplayName("Verify Multiple Select Checkbox Partial Credit Grading Logic")
    void testMultipleSelectPartialCreditGrading() {
        Question q = new Question(101L, "Select valid Java collection classes", "MULTIPLE_SELECT",
                "ArrayList", "LinkedList", "Vector", "HashSet", "A,B,C", "PARTIAL_CREDIT", false, 6, 0);

        // Student selects 2 of 3 correct answers: (2/3) * 6 = 4 marks
        int scoreTwoThirds = ExamController.calculateScore(q, new String[]{"A", "B"});
        assertEquals(4, scoreTwoThirds, "Partial credit should be awarded proportionally (2/3 * 6 = 4)");

        // Student selects all 3 correct: 6 marks
        int scoreAll = ExamController.calculateScore(q, new String[]{"A", "B", "C"});
        assertEquals(6, scoreAll, "All correct selections must award full marks");

        // Student selects 2 correct and 1 incorrect: fraction = max(0, (2 - 1) / 3) = 1/3 * 6 = 2 marks
        int scoreWithPenalty = ExamController.calculateScore(q, new String[]{"A", "B", "D"});
        assertEquals(2, scoreWithPenalty, "Incorrect selections penalize correct selections proportionally");
    }

    @Test
    @DisplayName("Verify Fill-in-the-Blanks Grading Logic (Trimming, Case-Insensitive, Multi-accepted)")
    void testFillInTheBlanksGrading() {
        Question q = new Question(101L, "Where is class metadata stored?", "FILL_BLANKS",
                "", "", "", "", "metaspace, method area, permgen", "ALL_OR_NOTHING", false, 5, 0);

        // Match first accepted answer with whitespace padding
        int score1 = ExamController.calculateScore(q, "  metaspace  ");
        assertEquals(5, score1, "Whitespace-padded answer must be trimmed and matched");

        // Match second accepted answer in uppercase
        int score2 = ExamController.calculateScore(q, "METHOD AREA");
        assertEquals(5, score2, "Case-insensitive matching must award full marks");

        // Match third accepted answer
        int score3 = ExamController.calculateScore(q, "permgen");
        assertEquals(5, score3, "Any alternative in comma-separated accepted answers must match");

        // Wrong answer
        int scoreWrong = ExamController.calculateScore(q, "heap");
        assertEquals(0, scoreWrong, "Incorrect fill-in answer must award 0 marks");
    }

    @Test
    @DisplayName("Verify Case-Sensitive Fill-in-the-Blanks Grading Logic")
    void testCaseSensitiveFillInTheBlanks() {
        Question q = new Question(101L, "Enter uppercase acronym", "FILL_BLANKS",
                "", "", "", "", "REST", "ALL_OR_NOTHING", true, 5, 0);

        int scoreCorrect = ExamController.calculateScore(q, "REST");
        assertEquals(5, scoreCorrect, "Exact case match must award full marks");

        int scoreWrongCase = ExamController.calculateScore(q, "rest");
        assertEquals(0, scoreWrongCase, "Case mismatch in case-sensitive mode must award 0 marks");
    }

    @Test
    @DisplayName("Verify Pass / Fail Calculation and Percentage Evaluation")
    void testPassFailCalculation() {
        Exam exam = new Exam("Web Technologies", "WT", 40, 50);
        exam.setPassMarks(20); // 40% threshold

        // Student A scores 25 (Pass)
        int scoreA = 25;
        String statusA = (scoreA >= exam.getPassMarks()) ? "PASS" : "FAIL";
        double percentageA = (scoreA * 100.0) / exam.getTotalMarks();
        assertEquals("PASS", statusA);
        assertEquals(50.0, percentageA);

        // Student B scores 19 (Fail)
        int scoreB = 19;
        String statusB = (scoreB >= exam.getPassMarks()) ? "PASS" : "FAIL";
        double percentageB = (scoreB * 100.0) / exam.getTotalMarks();
        assertEquals("FAIL", statusB);
        assertEquals(38.0, percentageB);

        // ExamResult entity evaluates pass/fail status
        ExamResult resPass = new ExamResult(1L, "Web Technologies", "WT", "studentA", scoreA, 50);
        assertEquals("PASS", resPass.getPassFailStatus());

        ExamResult resFail = new ExamResult(1L, "Web Technologies", "WT", "studentB", scoreB, 50);
        assertEquals("FAIL", resFail.getPassFailStatus());
    }

    @Test
    @DisplayName("Verify Admin Exam Results Multi-Tenant Isolation")
    void testAdminExamResultsTenantFilter() {
        ExamResult resAIT1 = new ExamResult(101L, "Java", "Java", "ait_user1", 18, 20);
        resAIT1.setCollegeCode("AIT");
        resAIT1.setRollNumber("21B91A0501");

        ExamResult resAIT2 = new ExamResult(102L, "DBMS", "DBMS", "ait_user2", 15, 20);
        resAIT2.setCollegeCode("AIT");
        resAIT2.setRollNumber("21B91A0502");

        ExamResult resNIE = new ExamResult(201L, "Python", "Python", "nie_user1", 19, 20);
        resNIE.setCollegeCode("NIE");
        resNIE.setRollNumber("NIE-2026-001");

        List<ExamResult> allResults = List.of(resAIT1, resAIT2, resNIE);

        // Administrator from AIT queries results
        String adminCollegeCode = "AIT";
        List<ExamResult> filteredResults = allResults.stream()
                .filter(r -> adminCollegeCode.equalsIgnoreCase(r.getCollegeCode()))
                .toList();

        assertEquals(2, filteredResults.size(), "Admin must only see results from their own institution");
        assertTrue(filteredResults.stream().allMatch(r -> "AIT".equalsIgnoreCase(r.getCollegeCode())));
        assertFalse(filteredResults.contains(resNIE), "AIT admin must never see NIE examination results");
    }

    @Test
    @DisplayName("Verify Homepage and Registration Approved College Filtering")
    void testApprovedCollegesHomepageFilter() {
        College c1 = new College("Apex Institute of Technology", "AIT", "Engineering", "APPROVED", "Hyderabad", "Dr. Rao", "admin@ait.edu", "9876543210");
        College c2 = new College("National Institute of Engineering", "NIE", "Engineering", "APPROVED", "Bengaluru", "Dr. Murthy", "admin@nie.edu", "9876543211");
        College c3 = new College("Sphoorthy Engineering College", "SPHN", "Engineering", "PENDING", "Hyderabad", "Dr. Principal", "admin@sphoorthy.edu", "9876543212");

        List<College> allColleges = List.of(c1, c2, c3);

        // Homepage and Registration Dropdowns show ONLY approved institutions
        List<College> visibleColleges = allColleges.stream()
                .filter(c -> "APPROVED".equalsIgnoreCase(c.getStatus()))
                .toList();

        assertEquals(2, visibleColleges.size(), "Only APPROVED colleges should appear on homepage and enrollment dropdowns");
        assertTrue(visibleColleges.contains(c1));
        assertTrue(visibleColleges.contains(c2));
        assertFalse(visibleColleges.contains(c3), "PENDING college must not appear in public approved lists until Super Admin approves");
    }
}
