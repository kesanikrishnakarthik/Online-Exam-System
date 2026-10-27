package com.onlineexam.online_exam;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OnboardingController {

    private final CollegeRepository collegeRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public OnboardingController(
            CollegeRepository collegeRepository,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder) {
        this.collegeRepository = collegeRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. College Registration Workflow
    @GetMapping("/register-college")
    public String showCollegeRegistrationForm() {
        return "register-college";
    }

    @PostMapping("/register-college")
    public String registerCollege(
            @RequestParam String name,
            @RequestParam String code,
            @RequestParam String type,
            @RequestParam String address,
            @RequestParam String contactPerson,
            @RequestParam String contactEmail,
            @RequestParam String contactPhone,
            RedirectAttributes redirectAttributes) {

        String trimmedCode = (code != null) ? code.trim().toUpperCase() : "";

        if (name.isBlank() || trimmedCode.isBlank() || contactEmail.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "All required fields must be filled.");
            return "redirect:/register-college";
        }

        if (collegeRepository.existsByCode(trimmedCode)) {
            redirectAttributes.addFlashAttribute("errorMessage", "College code '" + trimmedCode + "' is already registered.");
            return "redirect:/register-college";
        }

        College college = new College(name.trim(), trimmedCode, type, "PENDING",
                address, contactPerson, contactEmail, contactPhone);
        collegeRepository.save(college);

        redirectAttributes.addFlashAttribute("successMessage",
                "College application for '" + name + "' submitted successfully! It is pending Platform Administrator approval.");
        return "redirect:/login";
    }

    // 2. Student Registration Workflow
    @GetMapping("/register-student")
    public String showStudentRegistrationForm(Model model) {
        List<College> approvedColleges = collegeRepository.findByStatus("APPROVED");
        model.addAttribute("colleges", approvedColleges);
        return "register-student";
    }

    @PostMapping("/register-student")
    public String registerStudent(
            @RequestParam String collegeCode,
            @RequestParam String rollNumber,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String department,
            @RequestParam(required = false, defaultValue = "B.Tech CSE") String degreeBranch,
            @RequestParam(required = false, defaultValue = "3rd Year") String academicYear,
            @RequestParam(required = false, defaultValue = "1st Semester") String semester,
            @RequestParam(required = false, defaultValue = "A") String section,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        String trimmedCode = (collegeCode != null) ? collegeCode.trim().toUpperCase() : "";
        String trimmedRoll = (rollNumber != null) ? rollNumber.trim() : "";

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match.");
            return "redirect:/register-student";
        }

        College college = collegeRepository.findByCode(trimmedCode).orElse(null);
        if (college == null || !"APPROVED".equalsIgnoreCase(college.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Selected college is not approved or invalid.");
            return "redirect:/register-student";
        }

        // Generate student username: e.g. "AIT_21B91A0501" or if roll number unique, use rollNumber
        String username = trimmedCode.toLowerCase() + "_" + trimmedRoll.toLowerCase();
        if (userAccountRepository.existsByUsername(username)) {
            redirectAttributes.addFlashAttribute("errorMessage", "An account with roll number '" + trimmedRoll + "' already exists for this college.");
            return "redirect:/register-student";
        }

        UserAccount student = new UserAccount(
                username, passwordEncoder.encode(password), fullName, email,
                trimmedRoll, "ROLE_STUDENT", trimmedCode, college.getName(), department
        );
        student.setDegreeBranch(degreeBranch);
        student.setAcademicYear(academicYear);
        student.setSemester(semester);
        student.setSection(section);
        student.setStatus("ACTIVE");
        userAccountRepository.save(student);

        redirectAttributes.addFlashAttribute("successMessage",
                "Student account registered! You can now log in using username '" + username + "' or your Roll Number '" + trimmedRoll + "'.");
        return "redirect:/login";
    }

    // 3. Faculty Registration Workflow
    @GetMapping("/register-faculty")
    public String showFacultyRegistrationForm(Model model) {
        List<College> approvedColleges = collegeRepository.findByStatus("APPROVED");
        model.addAttribute("colleges", approvedColleges);
        return "register-faculty";
    }

    @PostMapping("/register-faculty")
    public String registerFaculty(
            @RequestParam String collegeCode,
            @RequestParam String facultyId,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String department,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {

        String trimmedCode = (collegeCode != null) ? collegeCode.trim().toUpperCase() : "";
        String trimmedId = (facultyId != null) ? facultyId.trim() : "";

        College college = collegeRepository.findByCode(trimmedCode).orElse(null);
        if (college == null || !"APPROVED".equalsIgnoreCase(college.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Selected college is not approved or invalid.");
            return "redirect:/register-faculty";
        }

        String username = trimmedCode.toLowerCase() + "_" + trimmedId.toLowerCase();
        if (userAccountRepository.existsByUsername(username)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Faculty ID already exists for this college.");
            return "redirect:/register-faculty";
        }

        UserAccount faculty = new UserAccount(
                username, passwordEncoder.encode(password), fullName, email,
                trimmedId, "ROLE_FACULTY", trimmedCode, college.getName(), department
        );
        faculty.setStatus("ACTIVE");
        userAccountRepository.save(faculty);

        redirectAttributes.addFlashAttribute("successMessage",
                "Faculty account created! You can now log in with username '" + username + "'.");
        return "redirect:/login";
    }
}

