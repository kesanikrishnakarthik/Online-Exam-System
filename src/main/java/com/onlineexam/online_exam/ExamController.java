package com.onlineexam.online_exam;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ExamController {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamResultRepository examResultRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final UserAccountRepository userAccountRepository;

    public ExamController(
            ExamRepository examRepository,
            QuestionRepository questionRepository,
            ExamResultRepository examResultRepository,
            ExamAttemptRepository examAttemptRepository,
            UserAccountRepository userAccountRepository) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.examResultRepository = examResultRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.userAccountRepository = userAccountRepository;
    }

    private UserAccount getAuthenticatedAccount(Authentication authentication) {
        if (authentication == null) return null;
        return userAccountRepository.findByUsername(authentication.getName()).orElse(null);
    }

    @GetMapping("/exams")
    public String availableExams(
            @RequestParam(required = false) String error,
            Authentication authentication,
            Model model) {

        String studentUsername = (authentication != null) ? authentication.getName() : "";
        UserAccount account = getAuthenticatedAccount(authentication);
        String collegeCode = (account != null && account.getCollegeCode() != null) ? account.getCollegeCode() : "";
        String collegeName = (account != null && account.getCollegeName() != null) ? account.getCollegeName() : "Institutional Examination Portal";

        // Multi-tenant isolation: Only fetch exams for student's own college
        List<Exam> exams = examRepository.findByCollegeCode(collegeCode);

        Map<Long, Integer> attemptsUsedMap = new HashMap<>();
        Map<Long, Integer> remainingAttemptsMap = new HashMap<>();
        Map<Long, Boolean> canTakeExamMap = new HashMap<>();

        for (Exam exam : exams) {
            int used = (int) examAttemptRepository.countByExamIdAndStudentUsername(exam.getId(), studentUsername);
            int max = exam.getMaxAttempts();
            int remaining = Math.max(0, max - used);

            attemptsUsedMap.put(exam.getId(), used);
            remainingAttemptsMap.put(exam.getId(), remaining);
            canTakeExamMap.put(exam.getId(), remaining > 0);
        }

        model.addAttribute("exams", exams);
        model.addAttribute("collegeName", collegeName);
        model.addAttribute("collegeCode", collegeCode);
        model.addAttribute("attemptsUsedMap", attemptsUsedMap);
        model.addAttribute("remainingAttemptsMap", remainingAttemptsMap);
        model.addAttribute("canTakeExamMap", canTakeExamMap);

        if ("attempts_exhausted".equals(error)) {
            model.addAttribute("errorMessage", "You have used all three attempts for this examination.");
        } else if ("unauthorized_college".equals(error)) {
            model.addAttribute("errorMessage", "Access Denied: This examination belongs to another institution.");
        }

        return "exams";
    }

    @GetMapping("/exam/{examId}")
    public String startExam(
            @PathVariable Long examId,
            Authentication authentication,
            Model model) {

        String studentUsername = (authentication != null) ? authentication.getName() : "";
        UserAccount account = getAuthenticatedAccount(authentication);
        String collegeCode = (account != null && account.getCollegeCode() != null) ? account.getCollegeCode() : "";
        String collegeName = (account != null && account.getCollegeName() != null) ? account.getCollegeName() : "Institutional Examination Portal";

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found: " + examId));

        // Strict Multi-Tenant Isolation Check
        if (!collegeCode.equalsIgnoreCase(exam.getCollegeCode())) {
            return "redirect:/exams?error=unauthorized_college";
        }

        // Strict attempt limit check (Atomic server validation)
        long usedAttempts = examAttemptRepository.countByExamIdAndStudentUsername(examId, studentUsername);
        if (usedAttempts >= exam.getMaxAttempts()) {
            return "redirect:/exams?error=attempts_exhausted";
        }

        List<Question> questions = questionRepository.findByExamId(examId);

        int totalMarks = questions.stream()
                .mapToInt(Question::getMarks)
                .sum();

        // Get or initialize active attempt
        Optional<ExamAttempt> inProgressAttempt = examAttemptRepository
                .findFirstByExamIdAndStudentUsernameAndStatus(examId, studentUsername, "IN_PROGRESS");

        ExamAttempt attempt;
        if (inProgressAttempt.isPresent()) {
            attempt = inProgressAttempt.get();
        } else {
            int nextAttemptNum = (int) usedAttempts + 1;
            attempt = new ExamAttempt(
                    exam.getId(),
                    exam.getTitle(),
                    exam.getSubject(),
                    studentUsername,
                    collegeCode,
                    collegeName,
                    nextAttemptNum,
                    LocalDateTime.now(),
                    "IN_PROGRESS"
            );
            attempt.setTotalMarks(totalMarks);
            examAttemptRepository.save(attempt);
        }

        model.addAttribute("exam", exam);
        model.addAttribute("collegeName", collegeName);
        model.addAttribute("collegeCode", collegeCode);
        model.addAttribute("questions", questions);
        model.addAttribute("totalMarks", totalMarks);
        model.addAttribute("passMarks", exam.getPassMarks());
        model.addAttribute("attempt", attempt);
        model.addAttribute("attemptNumber", attempt.getAttemptNumber());
        model.addAttribute("maxAttempts", exam.getMaxAttempts());
        model.addAttribute("maxViolations", exam.getMaxViolations());

        return "exam";
    }

    @PostMapping("/exam/{examId}/submit")
    public String submitExam(
            @PathVariable Long examId,
            @RequestParam(required = false, defaultValue = "NORMAL") String submissionReason,
            @RequestParam(required = false, defaultValue = "0") int violationCount,
            HttpServletRequest request,
            Authentication authentication,
            Model model) {

        String studentUsername = (authentication != null) ? authentication.getName() : "";
        UserAccount account = getAuthenticatedAccount(authentication);
        String collegeCode = (account != null && account.getCollegeCode() != null) ? account.getCollegeCode() : "";
        String collegeName = (account != null && account.getCollegeName() != null) ? account.getCollegeName() : "Institutional Examination Portal";
        String rollNumber = (account != null && account.getRollNumberOrFacultyId() != null) ? account.getRollNumberOrFacultyId() : studentUsername;

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found: " + examId));

        List<Question> questions = questionRepository.findByExamId(examId);

        int score = 0;
        int totalMarks = 0;

        for (Question question : questions) {
            totalMarks += question.getMarks();
            score += evaluateQuestionScore(question, request);
        }

        // Finalize attempt
        Optional<ExamAttempt> inProgressAttempt = examAttemptRepository
                .findFirstByExamIdAndStudentUsernameAndStatus(examId, studentUsername, "IN_PROGRESS");

        int attemptNumber = 1;
        ExamAttempt attempt;
        if (inProgressAttempt.isPresent()) {
            attempt = inProgressAttempt.get();
            attemptNumber = attempt.getAttemptNumber();
            attempt.setScore(score);
            attempt.setTotalMarks(totalMarks);
            attempt.setSubmissionTime(LocalDateTime.now());
            attempt.setViolationCount(violationCount);
            attempt.setSubmissionReason(submissionReason);

            if ("VIOLATION_LIMIT".equalsIgnoreCase(submissionReason)) {
                attempt.setStatus("AUTO_SUBMITTED_VIOLATION");
            } else if ("TIMEOUT".equalsIgnoreCase(submissionReason)) {
                attempt.setStatus("AUTO_SUBMITTED_TIMEOUT");
            } else {
                attempt.setStatus("COMPLETED");
            }

            examAttemptRepository.save(attempt);
        }

        String passFailStatus = (score >= exam.getPassMarks()) ? "PASS" : "FAIL";

        // Save official audited result record with collegeCode, rollNumber, and pass/fail evaluation
        ExamResult result = new ExamResult(
                exam.getId(),
                exam.getTitle(),
                exam.getSubject(),
                studentUsername,
                rollNumber,
                collegeCode,
                collegeName,
                score,
                totalMarks,
                attemptNumber,
                violationCount,
                submissionReason
        );
        result.setPassFailStatus(passFailStatus);
        examResultRepository.save(result);

        double percentage = totalMarks > 0 ? Math.round((score * 100.0 / totalMarks) * 100.0) / 100.0 : 0.0;

        model.addAttribute("exam", exam);
        model.addAttribute("score", score);
        model.addAttribute("totalMarks", totalMarks);
        model.addAttribute("percentage", percentage);
        model.addAttribute("passFailStatus", passFailStatus);
        model.addAttribute("passMarks", exam.getPassMarks());
        model.addAttribute("attemptNumber", attemptNumber);
        model.addAttribute("maxAttempts", exam.getMaxAttempts());
        model.addAttribute("submissionReason", submissionReason);
        model.addAttribute("violationCount", violationCount);
        model.addAttribute("collegeName", collegeName);

        return "exam-result";
    }

    /**
     * Automatic Server-Side Grading Engine Supporting:
     * 1. Single-Choice MCQ
     * 2. Multiple-Select Checkboxes (All-or-Nothing or Partial Credit)
     * 3. Fill-in-the-Blanks (Trimmed, Case-Insensitive, Multi-accepted answers)
     * 4. Short-Answer (Keyword concepts evaluation)
     */
    public static int calculateScore(Question question, String textAnswer) {
        return calculateScore(question, textAnswer, null);
    }

    public static int calculateScore(Question question, String[] multiAnswers) {
        return calculateScore(question, null, multiAnswers);
    }

    public static int calculateScore(Question question, String textAnswer, String[] multiAnswers) {
        if (question == null) return 0;
        String type = question.getQuestionType();

        if ("MULTIPLE_SELECT".equalsIgnoreCase(type)) {
            if (multiAnswers == null || multiAnswers.length == 0) return 0;

            Set<String> studentAnswers = Arrays.stream(multiAnswers)
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .collect(Collectors.toSet());

            Set<String> correctAnswers = Arrays.stream(question.getCorrectAnswer().split(","))
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .collect(Collectors.toSet());

            if ("PARTIAL_CREDIT".equalsIgnoreCase(question.getMarkingScheme())) {
                long correctSelected = studentAnswers.stream().filter(correctAnswers::contains).count();
                long incorrectSelected = studentAnswers.stream().filter(a -> !correctAnswers.contains(a)).count();
                if (correctAnswers.isEmpty()) return 0;
                double fraction = Math.max(0.0, (double) (correctSelected - incorrectSelected) / correctAnswers.size());
                return (int) Math.round(fraction * question.getMarks());
            } else {
                // ALL_OR_NOTHING scheme: student set must match correct set exactly
                return studentAnswers.equals(correctAnswers) ? question.getMarks() : 0;
            }

        } else if ("FILL_BLANKS".equalsIgnoreCase(type)) {
            if (textAnswer == null || textAnswer.isBlank()) return 0;

            String studentText = textAnswer.trim();
            String[] accepted = question.getCorrectAnswer().split(",");

            for (String acc : accepted) {
                String target = acc.trim();
                if (question.isCaseSensitive()) {
                    if (studentText.equals(target)) return question.getMarks();
                } else {
                    if (studentText.equalsIgnoreCase(target)) return question.getMarks();
                }
            }
            return 0;

        } else if ("SHORT_ANSWER".equalsIgnoreCase(type)) {
            if (textAnswer == null || textAnswer.isBlank()) return 0;
            String studentText = textAnswer.trim();
            if (question.getCorrectAnswer() == null || question.getCorrectAnswer().isBlank()) {
                return studentText.length() >= 5 ? question.getMarks() : 0;
            }
            String[] acceptedKeywords = question.getCorrectAnswer().split(",");
            for (String kw : acceptedKeywords) {
                if (!kw.trim().isBlank() && studentText.toLowerCase().contains(kw.trim().toLowerCase())) {
                    return question.getMarks();
                }
            }
            return 0;

        } else {
            // SINGLE_CHOICE (MCQ)
            if (textAnswer != null && question.getCorrectAnswer() != null
                    && textAnswer.trim().equalsIgnoreCase(question.getCorrectAnswer().trim())) {
                return question.getMarks();
            }
            return 0;
        }
    }

    private int evaluateQuestionScore(Question question, HttpServletRequest request) {
        String paramName = "question_" + question.getId();
        String textAnswer = request.getParameter(paramName);
        String[] multiAnswers = request.getParameterValues(paramName);
        return calculateScore(question, textAnswer, multiAnswers);
    }

    @PostMapping("/api/exam/{examId}/violation")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> recordViolation(
            @PathVariable Long examId,
            @RequestParam(defaultValue = "1") int violationCount,
            @RequestParam(defaultValue = "TAB_SWITCH") String violationType,
            Authentication authentication) {

        String studentUsername = (authentication != null) ? authentication.getName() : "";
        Exam exam = examRepository.findById(examId).orElse(null);

        Map<String, Object> response = new HashMap<>();
        if (exam == null) {
            response.put("error", "Exam not found");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<ExamAttempt> inProgressAttempt = examAttemptRepository
                .findFirstByExamIdAndStudentUsernameAndStatus(examId, studentUsername, "IN_PROGRESS");

        if (inProgressAttempt.isPresent()) {
            ExamAttempt attempt = inProgressAttempt.get();
            attempt.setViolationCount(violationCount);
            examAttemptRepository.save(attempt);
        }

        boolean autoSubmitted = violationCount >= exam.getMaxViolations();

        response.put("status", "recorded");
        response.put("violationCount", violationCount);
        response.put("maxViolations", exam.getMaxViolations());
        response.put("autoSubmitted", autoSubmitted);

        return ResponseEntity.ok(response);
    }
}
