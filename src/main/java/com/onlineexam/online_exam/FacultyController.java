package com.onlineexam.online_exam;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FacultyController {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamResultRepository examResultRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final UserAccountRepository userAccountRepository;

    public FacultyController(
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

    private UserAccount getFacultyAccount(Authentication authentication) {
        if (authentication == null) return null;
        return userAccountRepository.findByUsername(authentication.getName()).orElse(null);
    }

    @GetMapping("/faculty/dashboard")
    public String facultyDashboard(Authentication authentication, Model model) {
        UserAccount account = getFacultyAccount(authentication);
        String collegeCode = (account != null && account.getCollegeCode() != null) ? account.getCollegeCode() : "AIT";
        String collegeName = (account != null && account.getCollegeName() != null) ? account.getCollegeName() : "Apex Institute of Technology";

        List<Exam> exams = examRepository.findByCollegeCode(collegeCode);
        long totalQuestions = questionRepository.count();
        List<ExamResult> results = examResultRepository.findByCollegeCode(collegeCode);

        model.addAttribute("collegeName", collegeName);
        model.addAttribute("collegeCode", collegeCode);
        model.addAttribute("totalExams", exams.size());
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("totalResults", results.size());
        model.addAttribute("totalAttempts", results.size());
        model.addAttribute("message", "Faculty Examination Management Portal");

        return "faculty-dashboard";
    }

    @GetMapping("/faculty/exams")
    public String manageExams(Authentication authentication, Model model) {
        UserAccount account = getFacultyAccount(authentication);
        String collegeCode = (account != null && account.getCollegeCode() != null) ? account.getCollegeCode() : "AIT";
        String collegeName = (account != null && account.getCollegeName() != null) ? account.getCollegeName() : "Apex Institute of Technology";

        List<Exam> exams = examRepository.findByCollegeCode(collegeCode);
        model.addAttribute("exams", exams);
        model.addAttribute("collegeName", collegeName);
        model.addAttribute("collegeCode", collegeCode);

        return "faculty-exams";
    }

    @GetMapping("/faculty/results")
    public String studentPerformance(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String student,
            Authentication authentication,
            Model model) {

        UserAccount account = getFacultyAccount(authentication);
        String collegeCode = (account != null && account.getCollegeCode() != null) ? account.getCollegeCode() : "AIT";
        String collegeName = (account != null && account.getCollegeName() != null) ? account.getCollegeName() : "Apex Institute of Technology";

        // Multi-tenant filter: Only results for faculty's own college
        List<ExamResult> results = examResultRepository.findByCollegeCode(collegeCode);

        if (subject != null && !subject.isBlank()) {
            results = results.stream()
                    .filter(r -> r.getSubject().equalsIgnoreCase(subject.trim()))
                    .toList();
        }

        if (student != null && !student.isBlank()) {
            results = results.stream()
                    .filter(r -> r.getStudentUsername().toLowerCase().contains(student.trim().toLowerCase())
                            || (r.getRollNumber() != null && r.getRollNumber().toLowerCase().contains(student.trim().toLowerCase())))
                    .toList();
        }

        model.addAttribute("results", results);
        model.addAttribute("selectedSubject", subject);
        model.addAttribute("selectedStudent", student);
        model.addAttribute("collegeName", collegeName);
        model.addAttribute("collegeCode", collegeCode);

        return "faculty-results";
    }

    @GetMapping("/faculty/results/export")
    public void exportResultsToCsv(Authentication authentication, HttpServletResponse response) throws IOException {
        UserAccount account = getFacultyAccount(authentication);
        String collegeCode = (account != null && account.getCollegeCode() != null) ? account.getCollegeCode() : "AIT";

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + collegeCode + "-exam-results.csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("Result ID,Exam Title,Subject,Roll Number,Student Username,College Code,College,Score,Total Marks,Percentage,Attempt,Violations,Submission Reason,Date");

        List<ExamResult> results = examResultRepository.findByCollegeCode(collegeCode);
        for (ExamResult r : results) {
            writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%.2f%%,%d,%d,\"%s\",\"%s\"%n",
                    r.getId(),
                    r.getExamTitle() != null ? r.getExamTitle().replace("\"", "\"\"") : "",
                    r.getSubject() != null ? r.getSubject().replace("\"", "\"\"") : "",
                    r.getRollNumber() != null ? r.getRollNumber() : "",
                    r.getStudentUsername(),
                    r.getCollegeCode() != null ? r.getCollegeCode() : "",
                    r.getCollege() != null ? r.getCollege().replace("\"", "\"\"") : "",
                    r.getScore(),
                    r.getTotalMarks(),
                    r.getPercentage(),
                    r.getAttemptNumber(),
                    r.getViolationCount(),
                    r.getSubmissionReason(),
                    r.getSubmissionTime() != null ? r.getSubmissionTime().toString() : ""
            );
        }
    }
}
