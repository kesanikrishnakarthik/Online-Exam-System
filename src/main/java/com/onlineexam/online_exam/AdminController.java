package com.onlineexam.online_exam;

import java.util.List;
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

    public AdminController(
            ExamRepository examRepository,
            ExamResultRepository examResultRepository,
            ExamAttemptRepository examAttemptRepository) {
        this.examRepository = examRepository;
        this.examResultRepository = examResultRepository;
        this.examAttemptRepository = examAttemptRepository;
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        long totalExams = examRepository.count();
        long totalResults = examResultRepository.count();
        long totalAttempts = examAttemptRepository.count();

        model.addAttribute("collegeName", "Apex Institute of Technology");
        model.addAttribute("collegeCode", "AIT-ENG-2026");
        model.addAttribute("totalExams", totalExams);
        model.addAttribute("totalResults", totalResults);
        model.addAttribute("totalAttempts", totalAttempts);
        model.addAttribute("activeStudents", 248);
        model.addAttribute("totalFaculty", 34);
        model.addAttribute("departments", List.of("Computer Science & Engineering", "Information Technology", "Electronics & Communication", "Data Science & AI"));

        return "admin-dashboard";
    }

    @GetMapping("/admin/audit-logs")
    public String auditLogs(Model model) {
        List<ExamAttempt> attempts = examAttemptRepository.findAll();
        model.addAttribute("attempts", attempts);
        return "admin-audit-logs";
    }

    @PostMapping("/admin/reset-attempts")
    public String resetAttempts(
            @RequestParam Long examId,
            @RequestParam String studentUsername) {

        List<ExamAttempt> attempts = examAttemptRepository.findByExamIdAndStudentUsername(examId, studentUsername);
        examAttemptRepository.deleteAll(attempts);

        return "redirect:/admin/audit-logs?success=attempts_reset";
    }
}

