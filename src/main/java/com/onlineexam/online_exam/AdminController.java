package com.onlineexam.online_exam;

import java.util.Arrays;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
                .map(String::trim)
                .map(String::toUpperCase)
                .orElse("AIT");
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Authentication authentication, Model model) {
        String collegeCode = getAdminCollegeCode(authentication);
        College college = collegeRepository.findByCode(collegeCode).orElse(null);

        String collegeName = (college != null) ? college.getName() : "College of Technology";
        List<Exam> exams = examRepository.findAll().stream()
                .filter(e -> collegeCode.equalsIgnoreCase(e.getCollegeCode()))
                .toList();
        List<ExamResult> results = examResultRepository.findAll().stream()
                .filter(r -> collegeCode.equalsIgnoreCase(r.getCollegeCode()))
                .toList();
        
        List<UserAccount> students = userAccountRepository.findAll().stream()
                .filter(u -> collegeCode.equalsIgnoreCase(u.getCollegeCode()) && "ROLE_STUDENT".equalsIgnoreCase(u.getRole()))
                .toList();
        List<UserAccount> facultyList = userAccountRepository.findAll().stream()
                .filter(u -> collegeCode.equalsIgnoreCase(u.getCollegeCode()) && "ROLE_FACULTY".equalsIgnoreCase(u.getRole()))
                .toList();

        long pendingFacultyCount = facultyList.stream().filter(f -> "PENDING".equalsIgnoreCase(f.getStatus())).count();

        List<String> depts = (college != null && college.getDepartments() != null)
                ? Arrays.stream(college.getDepartments().split(",")).map(String::trim).toList()
                : List.of("Computer Science & Engineering", "Information Technology", "Electronics & Communication");

        model.addAttribute("college", college);
        model.addAttribute("collegeName", collegeName);
        model.addAttribute("collegeCode", collegeCode);
        model.addAttribute("totalExams", exams.size());
        model.addAttribute("totalResults", results.size());
        model.addAttribute("activeStudents", students.size());
        model.addAttribute("totalFaculty", facultyList.size());
        model.addAttribute("pendingFacultyCount", pendingFacultyCount);
        model.addAttribute("departments", depts);
        model.addAttribute("students", students);
        model.addAttribute("facultyList", facultyList);

        return "admin-dashboard";
    }

    @GetMapping("/admin/results")
    public String adminResults(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String student,
            Authentication authentication,
            Model model) {

        String collegeCode = getAdminCollegeCode(authentication);
        College college = collegeRepository.findByCode(collegeCode).orElse(null);
        String collegeName = (college != null) ? college.getName() : "College of Technology";

        List<ExamResult> results = examResultRepository.findAll().stream()
                .filter(r -> collegeCode.equalsIgnoreCase(r.getCollegeCode()))
                .toList();

        if (subject != null && !subject.isBlank()) {
            String subFilter = subject.trim().toLowerCase();
            results = results.stream()
                    .filter(r -> r.getSubject() != null && r.getSubject().toLowerCase().contains(subFilter))
                    .toList();
        }

        if (student != null && !student.isBlank()) {
            String stuFilter = student.trim().toLowerCase();
            results = results.stream()
                    .filter(r -> (r.getStudentUsername() != null && r.getStudentUsername().toLowerCase().contains(stuFilter))
                            || (r.getRollNumber() != null && r.getRollNumber().toLowerCase().contains(stuFilter)))
                    .toList();
        }

        model.addAttribute("college", college);
        model.addAttribute("collegeName", collegeName);
        model.addAttribute("collegeCode", collegeCode);
        model.addAttribute("results", results);
        model.addAttribute("selectedSubject", subject);
        model.addAttribute("selectedStudent", student);

        return "admin-results";
    }

    @PostMapping("/admin/faculty/{id}/approve")
    public String approveFaculty(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String collegeCode = getAdminCollegeCode(authentication);
        UserAccount faculty = userAccountRepository.findById(id).orElse(null);

        if (faculty != null && collegeCode.equalsIgnoreCase(faculty.getCollegeCode())) {
            faculty.setStatus("ACTIVE");
            userAccountRepository.save(faculty);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Faculty member '" + faculty.getFullName() + "' (" + faculty.getUsername() + ") has been approved and activated.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized action or faculty account not found.");
        }

        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/faculty/{id}/reject")
    public String rejectFaculty(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String collegeCode = getAdminCollegeCode(authentication);
        UserAccount faculty = userAccountRepository.findById(id).orElse(null);

        if (faculty != null && collegeCode.equalsIgnoreCase(faculty.getCollegeCode())) {
            faculty.setStatus("REJECTED");
            userAccountRepository.save(faculty);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Faculty application for '" + faculty.getFullName() + "' has been rejected.");
        }

        return "redirect:/admin/dashboard";
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
