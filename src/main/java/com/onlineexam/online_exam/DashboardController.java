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
    private final UserAccountRepository userAccountRepository;

    public DashboardController(
            ExamRepository examRepository,
            ExamResultRepository examResultRepository,
            ExamAttemptRepository examAttemptRepository,
            UserAccountRepository userAccountRepository) {
        this.examRepository = examRepository;
        this.examResultRepository = examResultRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String studentUsername = (authentication != null) ? authentication.getName() : "";
        UserAccount student = userAccountRepository.findByUsername(studentUsername).orElse(null);

        String collegeCode = (student != null && student.getCollegeCode() != null) ? student.getCollegeCode() : "";
        String collegeName = (student != null && student.getCollegeName() != null) ? student.getCollegeName() : "Institutional Student Portal";
        String department = (student != null && student.getDepartment() != null) ? student.getDepartment() : "General";
        String rollNumber = (student != null && student.getRollNumberOrFacultyId() != null) ? student.getRollNumberOrFacultyId() : studentUsername;
        String fullName = (student != null && student.getFullName() != null) ? student.getFullName() : studentUsername;

        // Multi-tenant exams list for student's college
        List<Exam> exams = examRepository.findByCollegeCode(collegeCode);
        List<ExamResult> myResults = examResultRepository.findByStudentUsername(studentUsername);

        long totalAttempts = examAttemptRepository.findByStudentUsernameOrderBySubmissionTimeDesc(studentUsername).size();

        double avgPercentage = 0.0;
        if (!myResults.isEmpty()) {
            double totalPct = myResults.stream().mapToDouble(ExamResult::getPercentage).sum();
            avgPercentage = Math.round((totalPct / myResults.size()) * 100.0) / 100.0;
        }

        model.addAttribute("studentUsername", studentUsername);
        model.addAttribute("fullName", fullName);
        model.addAttribute("rollNumber", rollNumber);
        model.addAttribute("collegeName", collegeName);
        model.addAttribute("collegeCode", collegeCode);
        model.addAttribute("department", department);
        model.addAttribute("availableExamsCount", exams.size());
        model.addAttribute("completedExamsCount", myResults.size());
        model.addAttribute("totalAttemptsCount", totalAttempts);
        model.addAttribute("avgPercentage", avgPercentage);
        model.addAttribute("recentResults", myResults.stream().limit(3).toList());

        return "dashboard";
    }
}
