package com.onlineexam.online_exam;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CreateExamController {

    private final ExamRepository examRepository;
    private final UserAccountRepository userAccountRepository;

    public CreateExamController(
            ExamRepository examRepository,
            UserAccountRepository userAccountRepository) {
        this.examRepository = examRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping("/faculty/create-exam")
    public String showCreateExamPage(Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication != null) {
            UserAccount faculty = userAccountRepository.findByUsername(authentication.getName()).orElse(null);
            if (faculty != null && !"ROLE_PLATFORM_ADMIN".equalsIgnoreCase(faculty.getRole()) && !"ACTIVE".equalsIgnoreCase(faculty.getStatus())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Your faculty account is pending verification by your college administration. You cannot create examinations until approved.");
                return "redirect:/faculty/dashboard";
            }
        }
        return "create-exam";
    }

    @PostMapping("/faculty/create-exam")
    public String createExam(
            @RequestParam String title,
            @RequestParam String subject,
            @RequestParam int durationMinutes,
            @RequestParam(defaultValue = "3") int maxAttempts,
            @RequestParam(defaultValue = "3") int maxViolations,
            @RequestParam(required = false, defaultValue = "B.Tech CSE") String branchProgram,
            @RequestParam(required = false, defaultValue = "3rd Year") String academicYear,
            @RequestParam(required = false, defaultValue = "1st Semester") String semester,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (title.isBlank() || subject.isBlank()
                || durationMinutes < 1 || durationMinutes > 300) {
            redirectAttributes.addFlashAttribute("errorMessage", "Enter valid exam details.");
            return "redirect:/faculty/create-exam";
        }

        String username = (authentication != null) ? authentication.getName() : "";
        UserAccount faculty = userAccountRepository.findByUsername(username).orElse(null);

        if (faculty == null) {
            return "redirect:/login";
        }

        if (!"ROLE_PLATFORM_ADMIN".equalsIgnoreCase(faculty.getRole()) && !"ACTIVE".equalsIgnoreCase(faculty.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Your faculty account is pending verification by your college administration. You cannot create examinations until approved.");
            return "redirect:/faculty/dashboard";
        }

        String collegeCode = (faculty.getCollegeCode() != null) ? faculty.getCollegeCode() : "";
        String collegeName = (faculty.getCollegeName() != null) ? faculty.getCollegeName() : "";
        String department = (faculty.getDepartment() != null) ? faculty.getDepartment() : "General";

        Exam exam = new Exam(title.trim(), subject.trim(), durationMinutes, 0);
        exam.setCollegeCode(collegeCode);
        exam.setCollege(collegeName);
        exam.setDepartment(department);
        exam.setBranchProgram(branchProgram);
        exam.setAcademicYear(academicYear);
        exam.setSemester(semester);
        exam.setMaxAttempts(maxAttempts > 0 ? maxAttempts : 3);
        exam.setMaxViolations(maxViolations > 0 ? maxViolations : 3);
        exam.setLifecycleStatus("PUBLISHED");
        exam.setCreatedBy(faculty.getUsername());
        examRepository.save(exam);

        return "redirect:/faculty/exam/" + exam.getId() + "/add-question";
    }
}
