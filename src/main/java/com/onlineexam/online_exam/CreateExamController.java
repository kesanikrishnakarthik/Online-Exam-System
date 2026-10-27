package com.onlineexam.online_exam;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
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
    public String showCreateExamPage() {
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
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Enter valid exam details.");
            return "redirect:/faculty/create-exam";
        }

        String username = (authentication != null) ? authentication.getName() : "faculty";
        UserAccount faculty = userAccountRepository.findByUsername(username).orElse(null);
        String collegeCode = (faculty != null && faculty.getCollegeCode() != null) ? faculty.getCollegeCode() : "AIT";
        String collegeName = (faculty != null && faculty.getCollegeName() != null) ? faculty.getCollegeName() : "Apex Institute of Technology";
        String department = (faculty != null && faculty.getDepartment() != null) ? faculty.getDepartment() : "Computer Science & Engineering";

        Exam exam = new Exam(title, subject, durationMinutes, 0);
        exam.setCollegeCode(collegeCode);
        exam.setCollege(collegeName);
        exam.setDepartment(department);
        exam.setBranchProgram(branchProgram);
        exam.setAcademicYear(academicYear);
        exam.setSemester(semester);
        exam.setMaxAttempts(maxAttempts > 0 ? maxAttempts : 3);
        exam.setMaxViolations(maxViolations > 0 ? maxViolations : 3);
        exam.setLifecycleStatus("PUBLISHED");
        examRepository.save(exam);

        return "redirect:/faculty/exam/" + exam.getId() + "/add-question";
    }
}
