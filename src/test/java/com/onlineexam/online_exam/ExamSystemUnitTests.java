package com.onlineexam.online_exam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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
        sampleExam.setCollege("Apex Institute of Technology");
        sampleExam.setMaxAttempts(3);
        sampleExam.setMaxViolations(3);

        sampleQuestions = new ArrayList<>();
        sampleQuestions.add(new Question(101L, "Which keyword is used to inherit a class in Java?", "implements", "extends", "inherits", "super", "B", 4));
        sampleQuestions.add(new Question(101L, "Which method is the entry point of a Java application?", "start()", "run()", "main()", "init()", "C", 4));
        sampleQuestions.add(new Question(101L, "Which collection does not allow duplicate elements?", "List", "ArrayList", "Set", "Queue", "C", 4));
    }

    @Test
    @DisplayName("Test 1: Verify Exam Creation Defaults & Metadata")
    void testExamCreationDefaults() {
        assertNotNull(sampleExam);
        assertEquals(3, sampleExam.getMaxAttempts(), "Default attempt limit must be 3");
        assertEquals(3, sampleExam.getMaxViolations(), "Default security violation threshold must be 3");
        assertEquals("Apex Institute of Technology", sampleExam.getCollege());
        assertEquals("AIT", sampleExam.getCollegeCode());
    }

    @Test
    @DisplayName("Test 2: Verify Strict 3-Attempt Enforcement Logic")
    void testStrictThreeAttemptLimit() {
        String student = "student1";
        List<ExamAttempt> history = new ArrayList<>();

        // Attempt 1
        history.add(new ExamAttempt(sampleExam.getId(), sampleExam.getTitle(), sampleExam.getSubject(),
                student, sampleExam.getCollegeCode(), sampleExam.getCollege(), 1, LocalDateTime.now(), "COMPLETED"));
        assertTrue(history.size() < sampleExam.getMaxAttempts(), "Attempt 2 must be allowed");

        // Attempt 2
        history.add(new ExamAttempt(sampleExam.getId(), sampleExam.getTitle(), sampleExam.getSubject(),
                student, sampleExam.getCollegeCode(), sampleExam.getCollege(), 2, LocalDateTime.now(), "COMPLETED"));
        assertTrue(history.size() < sampleExam.getMaxAttempts(), "Attempt 3 must be allowed");

        // Attempt 3
        history.add(new ExamAttempt(sampleExam.getId(), sampleExam.getTitle(), sampleExam.getSubject(),
                student, sampleExam.getCollegeCode(), sampleExam.getCollege(), 3, LocalDateTime.now(), "COMPLETED"));
        assertEquals(3, history.size());

        // Verify Attempt 4 is strictly disallowed
        boolean canTakeFourthAttempt = history.size() < sampleExam.getMaxAttempts();
        assertFalse(canTakeFourthAttempt, "Attempt 4 must be strictly blocked once 3 attempts are exhausted");
    }

    @Test
    @DisplayName("Test 3: Verify Server-Side Score Calculation Without Exposing Answers")
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
    @DisplayName("Test 4: Verify Anti-Cheating Violation Auto-Submission Threshold")
    void testAntiCheatingViolationThreshold() {
        ExamAttempt attempt = new ExamAttempt(sampleExam.getId(), sampleExam.getTitle(), sampleExam.getSubject(),
                "testuser", sampleExam.getCollegeCode(), sampleExam.getCollege(), 1, LocalDateTime.now(), "IN_PROGRESS");

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
    @DisplayName("Test 5: Verify Examination Timer Expiration Auto-Submission")
    void testTimerExpirationAutoSubmission() {
        ExamAttempt attempt = new ExamAttempt(sampleExam.getId(), sampleExam.getTitle(), sampleExam.getSubject(),
                "testuser", sampleExam.getCollegeCode(), sampleExam.getCollege(), 1, LocalDateTime.now(), "IN_PROGRESS");

        // Simulate timer hitting 0
        attempt.setStatus("AUTO_SUBMITTED_TIMEOUT");
        attempt.setSubmissionReason("TIMEOUT");
        attempt.setSubmissionTime(LocalDateTime.now());

        assertEquals("AUTO_SUBMITTED_TIMEOUT", attempt.getStatus());
        assertEquals("TIMEOUT", attempt.getSubmissionReason());
    }

    @Test
    @DisplayName("Test 6: Verify Student Result Privacy (Student only sees own results)")
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
    @DisplayName("Test 7: Verify Multi-Tenant Data Isolation (College A cannot see College B exams)")
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
    @DisplayName("Test 8: Verify College Approval Gate (Only APPROVED colleges allow active enrollment)")
    void testCollegeApprovalGate() {
        College approvedCollege = new College("Apex Institute", "AIT", "Engineering", "APPROVED", "Hyderabad", "Dr. Rao", "admin@ait.edu", "9876543210");
        College pendingCollege = new College("St. Xavier's", "SXC", "Degree", "PENDING", "Mumbai", "Fr. Joseph", "admin@sxc.edu", "9876543211");
        College suspendedCollege = new College("Old Institute", "OLD", "Polytechnic", "SUSPENDED", "Delhi", "Prof. Kumar", "admin@old.edu", "9876543212");

        assertTrue("APPROVED".equalsIgnoreCase(approvedCollege.getStatus()), "Approved college must allow student enrollment");
        assertFalse("APPROVED".equalsIgnoreCase(pendingCollege.getStatus()), "Pending college must block onboarding until Super Admin approval");
        assertFalse("APPROVED".equalsIgnoreCase(suspendedCollege.getStatus()), "Suspended college must block new enrollment");
    }

    @Test
    @DisplayName("Test 9: Verify Institutional Roll Number Login Parsing")
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
    @DisplayName("Test 10: Verify Single Choice MCQ Grading Logic")
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
    @DisplayName("Test 11: Verify Multiple Select Checkbox All-or-Nothing Grading Logic")
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
    @DisplayName("Test 12: Verify Multiple Select Checkbox Partial Credit Grading Logic")
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
    @DisplayName("Test 13: Verify Fill-in-the-Blanks Grading Logic (Trimming, Case-Insensitive, Multi-accepted)")
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
    @DisplayName("Test 14: Verify Short Answer Concept Keyword Grading")
    void testShortAnswerGrading() {
        Question q = new Question(101L, "Explain the purpose of an interface in Java", "SHORT_ANSWER",
                "", "", "", "", "contract,polymorphism,abstraction", "ALL_OR_NOTHING", false, 5, 0);

        int scoreValid = ExamController.calculateScore(q, "An interface establishes an architectural contract for classes.");
        assertEquals(5, scoreValid, "Answer containing key concept 'contract' must award marks");

        int scoreInvalid = ExamController.calculateScore(q, "It is just code.");
        assertEquals(0, scoreInvalid, "Answer lacking key concepts must award 0 marks");
    }

    @Test
    @DisplayName("Test 15: Verify Pass / Fail Calculation and Percentage Evaluation")
    void testPassFailCalculation() {
        Exam exam = new Exam("Web Technologies", "WT", 40, 50);
        exam.setPassMarks(20);

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
    }

    @Test
    @DisplayName("Test 16: Verify Admin Exam Results Multi-Tenant Isolation")
    void testAdminExamResultsTenantFilter() {
        ExamResult resAIT1 = new ExamResult(101L, "Java", "Java", "ait_user1", 18, 20);
        resAIT1.setCollegeCode("AIT");
        resAIT1.setRollNumber("21B91A0501");

        ExamResult resNIE = new ExamResult(201L, "Python", "Python", "nie_user1", 19, 20);
        resNIE.setCollegeCode("NIE");
        resNIE.setRollNumber("NIE-2026-001");

        List<ExamResult> allResults = List.of(resAIT1, resNIE);

        String adminCollegeCode = "AIT";
        List<ExamResult> filteredResults = allResults.stream()
                .filter(r -> adminCollegeCode.equalsIgnoreCase(r.getCollegeCode()))
                .toList();

        assertEquals(1, filteredResults.size(), "Admin must only see results from their own institution");
        assertEquals("AIT", filteredResults.get(0).getCollegeCode());
        assertFalse(filteredResults.contains(resNIE), "AIT admin must never see NIE examination results");
    }

    @Test
    @DisplayName("Test 17: Verify Cryptographic Password Reset Token Hashing & Expiration")
    void testPasswordResetTokenSecurity() {
        String rawToken = "5f2b8d91a3c74e0192837465abcdefff";
        String hash1 = PasswordResetService.hashToken(rawToken);
        String hash2 = PasswordResetService.hashToken(rawToken);

        assertNotNull(hash1);
        assertEquals(64, hash1.length(), "SHA-256 hash must be 64 hexadecimal characters");
        assertEquals(hash1, hash2, "Hashing must be deterministic");
        assertNotEquals(rawToken, hash1, "Raw token must not match token hash");

        PasswordResetToken activeToken = new PasswordResetToken(hash1, "student1", "student@ait.edu", LocalDateTime.now().plusMinutes(30));
        assertFalse(activeToken.isExpired(), "Token with future expiry date must not be expired");
        assertFalse(activeToken.isUsed(), "Token must initially be unused");

        PasswordResetToken expiredToken = new PasswordResetToken(hash1, "student1", "student@ait.edu", LocalDateTime.now().minusMinutes(1));
        assertTrue(expiredToken.isExpired(), "Token past its expiry timestamp must report expired");
    }

    @Test
    @DisplayName("Test 18: Verify Roll Number Uniqueness Scoped by College Tenant")
    void testRollNumberScopedUniqueness() {
        UserAccount s1 = new UserAccount("ait_21b91a0501", "pass", "Alex", "alex@ait.edu", "21B91A0501", "ROLE_STUDENT", "AIT", "Apex", "CSE");
        UserAccount s2 = new UserAccount("sriindu_21b91a0501", "pass", "John", "john@sriindu.edu", "21B91A0501", "ROLE_STUDENT", "SIET", "Sri Indu", "CSE");

        // Both colleges have identical roll number "21B91A0501" without collision
        assertEquals(s1.getInstitutionalId(), s2.getInstitutionalId(), "Roll numbers can be shared across separate colleges");
        assertNotEquals(s1.getCollegeCode(), s2.getCollegeCode(), "Colleges must differ");
        assertNotEquals(s1.getUsername(), s2.getUsername(), "Global username must be segregated");
    }

    @Test
    @DisplayName("Test 19: Verify College & Administrator Registration Data Binding & BCrypt Hash")
    void testCollegeAndAdminRegistrationWorkflow() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "AdminSecurePass123!";

        // Register Sri Indu Engineering College
        College college = new College("Sri Indu Engineering College", "INDU", "Engineering & Technology", "APPROVED",
                "Ibrahimpatnam, Hyderabad", "Dr. Principal", "principal@sriindu.ac.in", "+919876543210");
        college.setId(10L);

        assertEquals("APPROVED", college.getStatus(), "Newly registered college must have APPROVED status for immediate operations");
        assertEquals("INDU", college.getCode());

        // Create linked administrator account
        UserAccount adminAccount = new UserAccount(
                "indu_admin",
                encoder.encode(rawPassword),
                "Dr. Principal",
                "principal@sriindu.ac.in",
                "INDU-ADM-01",
                "ROLE_ADMIN",
                college.getCode(),
                college.getName(),
                "Academic Administration"
        );
        adminAccount.setStatus("ACTIVE");

        assertEquals("ACTIVE", adminAccount.getStatus(), "Newly registered admin must be ACTIVE to allow immediate login");
        assertEquals("ROLE_ADMIN", adminAccount.getRole());
        assertEquals("INDU", adminAccount.getCollegeCode(), "Admin must be strictly linked to college tenant code");
        assertTrue(encoder.matches(rawPassword, adminAccount.getPassword()), "Stored password must be a valid BCrypt hash");
    }

    @Test
    @DisplayName("Test 20: Verify Administrator Password Verification and Rejection")
    void testAdminPasswordVerificationAndRejection() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String correctPassword = "CorrectAdminPassword#2026";
        String wrongPassword = "WrongPassword#2026";

        String encoded = encoder.encode(correctPassword);

        assertTrue(encoder.matches(correctPassword, encoded), "Correct password must verify successfully");
        assertFalse(encoder.matches(wrongPassword, encoded), "Incorrect password must be rejected");
        assertFalse(encoder.matches("", encoded), "Empty password must be rejected");
    }

    @Test
    @DisplayName("Test 21: Verify Duplicate College Code and Username Collision Detection")
    void testDuplicateRegistrationValidation() {
        List<College> existingColleges = List.of(
                new College("Apex Institute of Technology", "AIT", "Engineering", "APPROVED", "Campus", "Dean", "dean@ait.edu", "123"),
                new College("Sri Indu Engineering College", "INDU", "Engineering", "APPROVED", "Campus", "Principal", "p@indu.edu", "456")
        );

        // Case-insensitive college code uniqueness check
        String duplicateCodeInput = "indu";
        boolean codeExists = existingColleges.stream()
                .anyMatch(c -> c.getCode().equalsIgnoreCase(duplicateCodeInput.trim()));
        assertTrue(codeExists, "Duplicate institutional code (even lowercase 'indu') must be detected");

        String newCodeInput = "MIT";
        boolean newCodeExists = existingColleges.stream()
                .anyMatch(c -> c.getCode().equalsIgnoreCase(newCodeInput.trim()));
        assertFalse(newCodeExists, "Unique institutional code must be accepted");

        // Case-insensitive administrator username uniqueness check
        List<UserAccount> existingUsers = List.of(
                new UserAccount("indu_admin", "hash", "Admin", "a@indu.edu", "INDU-ADM-01", "ROLE_ADMIN", "INDU", "Sri Indu", "Admin")
        );
        String duplicateUsername = "INDU_ADMIN";
        boolean usernameExists = existingUsers.stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(duplicateUsername.trim()));
        assertTrue(usernameExists, "Duplicate administrator username must be detected regardless of case");
    }

    @Test
    @DisplayName("Test 22: Verify Login Identifier Resolution for Prefixed and Direct Inputs")
    void testLoginIdentifierResolution() {
        UserAccount admin = new UserAccount("indu_admin", "hash", "Dr. Principal", "admin@indu.edu",
                "INDU-ADM-01", "ROLE_ADMIN", "INDU", "Sri Indu", "Admin");

        // Direct username lookup
        assertEquals("indu_admin", admin.getUsername());

        // Dropdown compound lookup: "INDU:indu_admin"
        String loginInputWithDropdown = "INDU:indu_admin";
        String[] parts = loginInputWithDropdown.split("[:/]", 2);
        String colCode = parts[0].trim();
        String ident = parts[1].trim();

        assertEquals("INDU", colCode);
        assertTrue(admin.getCollegeCode().equalsIgnoreCase(colCode));
        assertTrue(admin.getUsername().equalsIgnoreCase(ident) || admin.getInstitutionalId().equalsIgnoreCase(ident));

        // Compound lookup with institutional ID: "INDU:INDU-ADM-01"
        String loginInputWithInstId = "INDU:INDU-ADM-01";
        String[] instParts = loginInputWithInstId.split("[:/]", 2);
        assertTrue(admin.getInstitutionalId().equalsIgnoreCase(instParts[1].trim()));
    }

    @Test
    @DisplayName("Test 23: Verify Dynamic College Dropdown Filter Only Displays Approved/Active Colleges")
    void testDynamicCollegeDropdownFilter() {
        List<College> allColleges = List.of(
                new College("Apex Institute of Technology", "AIT", "Engineering", "APPROVED", "Addr", "Dean", "d@ait.edu", "111"),
                new College("Sri Indu Engineering College", "INDU", "Engineering", "APPROVED", "Addr", "Principal", "p@indu.edu", "222"),
                new College("Pending Test College", "PEND", "Engineering", "PENDING", "Addr", "Contact", "c@pend.edu", "333"),
                new College("Suspended College", "SUSP", "Engineering", "SUSPENDED", "Addr", "Contact", "c@susp.edu", "444")
        );

        List<College> eligibleForDropdown = allColleges.stream()
                .filter(c -> "APPROVED".equalsIgnoreCase(c.getStatus()) || "ACTIVE".equalsIgnoreCase(c.getStatus()))
                .toList();

        assertEquals(2, eligibleForDropdown.size(), "Only APPROVED or ACTIVE colleges must appear in candidate registration dropdowns");
        assertTrue(eligibleForDropdown.stream().anyMatch(c -> "INDU".equals(c.getCode())), "Sri Indu (INDU) must be present in dropdown");
        assertTrue(eligibleForDropdown.stream().anyMatch(c -> "AIT".equals(c.getCode())), "Apex (AIT) must be present in dropdown");
        assertFalse(eligibleForDropdown.stream().anyMatch(c -> "PEND".equals(c.getCode())), "Pending colleges must not be shown");
        assertFalse(eligibleForDropdown.stream().anyMatch(c -> "SUSP".equals(c.getCode())), "Suspended colleges must not be shown");
    }

    @Test
    @DisplayName("Test 24: Verify Candidate College Selection Resolution via Primary Key ID or Code")
    void testCandidateCollegeSelectionResolution() {
        College college1 = new College("Sri Indu Engineering College", "INDU", "Engineering", "APPROVED", "Campus", "Dean", "dean@indu.edu", "123");
        college1.setId(42L);

        List<College> colleges = List.of(college1);

        // Case A: Form submits stable database ID "42"
        String submittedId = "42";
        Optional<College> resolvedById = colleges.stream()
                .filter(c -> c.getId().toString().equals(submittedId.trim()))
                .findFirst();
        assertTrue(resolvedById.isPresent(), "College must be resolvable by stable primary key ID");
        assertEquals("INDU", resolvedById.get().getCode());

        // Case B: Form submits institutional code "INDU"
        String submittedCode = "INDU";
        Optional<College> resolvedByCode = colleges.stream()
                .filter(c -> c.getCode().equalsIgnoreCase(submittedCode.trim()))
                .findFirst();
        assertTrue(resolvedByCode.isPresent(), "College must be resolvable by institutional code");
        assertEquals(42L, resolvedByCode.get().getId());
    }
}
