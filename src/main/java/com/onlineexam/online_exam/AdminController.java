package com.onlineexam.online_exam;

import java.util.Arrays;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final UserAccountRepository userAccountRepository;
    private final CollegeRepository collegeRepository;

    public AdminController(
            ExamRepository examRepository,
            ExamResultRepository examResultRepository,
            ExamAttemptRepository examAttemptRepository,
            UserAccountRepository userAccountRepository,
            CollegeRepository collegeRepository) {
        this.examRepository = examRepository;
        this.examResultRepository = examResultRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.userAccountRepository = userAccountRepository;
        this.collegeRepository = collegeRepository;
    }

    private String getAdminCollegeCode(Authentication authentication) {
        if (authentication == null) return "AIT";
        return userAccountRepository.findByUsername(authentication.getName())
                .map(UserAccount::getCollegeCode)
                .orElse("AIT");
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Authentication authentication, Model model) {
        String collegeCode = getAdminCollegeCode(authentication);
        College college = collegeRepository.findByCode(collegeCode).orElse(null);

        String collegeName = (college != null) ? college.getName() : "College of Technology";
        List<Exam> exams = examRepository.findByCollegeCode(collegeCode);
        List<ExamResult> results = examResultRepository.findByCollegeCode(collegeCode);
        long studentCount = userAccountRepository.findByCollegeCodeAndRole(collegeCode, "ROLE_STUDENT").size();
        long facultyCount = userAccountRepository.findByCollegeCodeAndRole(collegeCode, "ROLE_FACULTY").size();

        List<String> depts = (college != null && college.getDepartments() != null)
                ? Arrays.stream(college.getDepartments().split(",")).map(String::trim).toList()
                : List.of("Computer Science & Engineering", "Information Technology");

        model.addAttribute("collegeName", collegeName);
        model.addAttribute("collegeCode", collegeCode);
        model.addAttribute("totalExams", exams.size());
        model.addAttribute("totalResults", results.size());
        model.addAttribute("activeStudents", studentCount);
        model.addAttribute("totalFaculty", facultyCount);
        model.addAttribute("departments", depts);

        return "admin-dashboard";
    }

    @GetMapping("/admin/audit-logs")
    public String auditLogs(Authentication authentication, Model model) {
        String collegeCode = getAdminCollegeCode(authentication);
        List<ExamAttempt> attempts = examAttemptRepository.findByCollegeCodeOrderBySubmissionTimeDesc(collegeCode);

        model.addAttribute("attempts", attempts);
        model.addAttribute("collegeCode", collegeCode);
        return "admin-audit-logs";
    }

    @PostMapping("/admin/reset-attempts")
    public String resetAttempts(
            @RequestParam Long examId,
            @RequestParam String studentUsername,
            Authentication authentication) {

        String collegeCode = getAdminCollegeCode(authentication);
        List<ExamAttempt> attempts = examAttemptRepository.findByExamIdAndStudentUsername(examId, studentUsername).stream()
                .filter(a -> collegeCode.equalsIgnoreCase(a.getCollegeCode()))
                .toList();

        examAttemptRepository.deleteAll(attempts);

        return "redirect:/admin/audit-logs?success=attempts_reset";
    }
}
