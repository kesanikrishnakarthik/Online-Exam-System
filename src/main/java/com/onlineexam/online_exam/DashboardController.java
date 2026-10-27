package com.onlineexam.online_exam;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;
    private final ExamAttemptRepository examAttemptRepository;

    public DashboardController(
            ExamRepository examRepository,
            ExamResultRepository examResultRepository,
            ExamAttemptRepository examAttemptRepository) {
        this.examRepository = examRepository;
        this.examResultRepository = examResultRepository;
        this.examAttemptRepository = examAttemptRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String studentUsername = (authentication != null) ? authentication.getName() : "user";

        List<Exam> exams = examRepository.findAll();
        List<ExamResult> myResults = examResultRepository.findAll().stream()
                .filter(r -> r.getStudentUsername().equals(studentUsername))
                .toList();

        long totalAttempts = examAttemptRepository.findByStudentUsernameOrderBySubmissionTimeDesc(studentUsername).size();

        double avgPercentage = 0.0;
        if (!myResults.isEmpty()) {
            double totalPct = myResults.stream().mapToDouble(ExamResult::getPercentage).sum();
            avgPercentage = Math.round((totalPct / myResults.size()) * 100.0) / 100.0;
        }

        model.addAttribute("studentUsername", studentUsername);
        model.addAttribute("collegeName", "Apex Institute of Technology");
        model.addAttribute("department", "Computer Science & Engineering");
        model.addAttribute("availableExamsCount", exams.size());
        model.addAttribute("completedExamsCount", myResults.size());
        model.addAttribute("totalAttemptsCount", totalAttempts);
        model.addAttribute("avgPercentage", avgPercentage);
        model.addAttribute("recentResults", myResults.stream().limit(3).toList());

        return "dashboard";
    }
}
