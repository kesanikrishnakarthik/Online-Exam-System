package com.onlineexam.online_exam;

import java.util.List;
import org.springframework.security.core.Authentication;
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

    // =========================================================================
    // 1. College Onboarding with College Admin Account Generation
    // =========================================================================
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
            @RequestParam String adminUsername,
            @RequestParam String adminPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        String trimmedCode = (code != null) ? code.trim().toUpperCase() : "";
        String trimmedAdminUser = (adminUsername != null) ? adminUsername.trim().toLowerCase() : "";

        if (name.isBlank() || trimmedCode.isBlank() || contactEmail.isBlank() || trimmedAdminUser.isBlank() || adminPassword.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "All required fields, including Administrator credentials, must be provided.");
            return "redirect:/register-college";
        }

        if (!adminPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Administrator passwords do not match.");
            return "redirect:/register-college";
        }

        if (collegeRepository.existsByCode(trimmedCode)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Institutional code '" + trimmedCode + "' is already registered in the platform.");
            return "redirect:/register-college";
        }

        if (userAccountRepository.existsByUsername(trimmedAdminUser)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Administrator username '" + trimmedAdminUser + "' is already taken. Please choose another username.");
            return "redirect:/register-college";
        }

        // Save new college tenant with status PENDING
        College college = new College(name.trim(), trimmedCode, type, "PENDING",
                address, contactPerson, contactEmail, contactPhone);
        collegeRepository.save(college);

        // Save linked College Administrator account with status PENDING (activated on approval)
        UserAccount adminAccount = new UserAccount(
                trimmedAdminUser,
                passwordEncoder.encode(adminPassword),
                contactPerson,
                contactEmail,
                trimmedCode + "-ADM-01",
                "ROLE_ADMIN",
                trimmedCode,
                name.trim(),
                "Academic Administration"
        );
        adminAccount.setStatus("PENDING");
        userAccountRepository.save(adminAccount);

        redirectAttributes.addFlashAttribute("successMessage",
                "Institution '" + name + "' (Code: " + trimmedCode + ") registered successfully! The application and Administrator account ('" + trimmedAdminUser + "') are pending Platform Super Administrator review.");
        return "redirect:/login";
    }

    // =========================================================================
    // 2. Student Candidate Enrollment
    // =========================================================================
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

        if (fullName.isBlank() || trimmedRoll.isBlank() || email.isBlank() || password.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please complete all required fields.");
            return "redirect:/register-student";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match.");
            return "redirect:/register-student";
        }

        College college = collegeRepository.findByCode(trimmedCode).orElse(null);
        if (college == null || !"APPROVED".equalsIgnoreCase(college.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "The selected college is not currently approved for examination registration.");
            return "redirect:/register-student";
        }

        if (userAccountRepository.existsByCollegeCodeAndInstitutionalId(trimmedCode, trimmedRoll)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Candidate with Roll Number '" + trimmedRoll + "' is already enrolled under " + college.getName() + ".");
            return "redirect:/register-student";
        }

        // Standardized username: e.g. "ait_21b91a0501"
        String username = trimmedCode.toLowerCase() + "_" + trimmedRoll.toLowerCase();
        if (userAccountRepository.existsByUsername(username)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Username '" + username + "' already exists.");
            return "redirect:/register-student";
        }

        UserAccount student = new UserAccount(
                username, passwordEncoder.encode(password), fullName.trim(), email.trim(),
                trimmedRoll, "ROLE_STUDENT", trimmedCode, college.getName(), department
        );
        student.setDegreeBranch(degreeBranch);
        student.setAcademicYear(academicYear);
        student.setSemester(semester);
        student.setSection(section);
        student.setStatus("ACTIVE");
        userAccountRepository.save(student);

        redirectAttributes.addFlashAttribute("successMessage",
                "Enrollment successful! You can now log in using Roll Number '" + trimmedRoll + "' or handle '" + username + "'.");
        return "redirect:/login";
    }

    // =========================================================================
    // 3. Faculty Onboarding & Controlled Verification
    // =========================================================================
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
            @RequestParam(required = false, defaultValue = "Assistant Professor") String designation,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        String trimmedCode = (collegeCode != null) ? collegeCode.trim().toUpperCase() : "";
        String trimmedId = (facultyId != null) ? facultyId.trim() : "";

        if (fullName.isBlank() || trimmedId.isBlank() || email.isBlank() || password.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "All required faculty fields must be filled.");
            return "redirect:/register-faculty";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match.");
            return "redirect:/register-faculty";
        }

        College college = collegeRepository.findByCode(trimmedCode).orElse(null);
        if (college == null || !"APPROVED".equalsIgnoreCase(college.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Selected institution is not approved or active.");
            return "redirect:/register-faculty";
        }

        if (userAccountRepository.existsByCollegeCodeAndInstitutionalId(trimmedCode, trimmedId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Faculty ID '" + trimmedId + "' is already registered in " + college.getName() + ".");
            return "redirect:/register-faculty";
        }

        String username = trimmedCode.toLowerCase() + "_" + trimmedId.toLowerCase();
        if (userAccountRepository.existsByUsername(username)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Faculty account already exists.");
            return "redirect:/register-faculty";
        }

        UserAccount faculty = new UserAccount(
                username, passwordEncoder.encode(password), fullName.trim(), email.trim(),
                trimmedId, "ROLE_FACULTY", trimmedCode, college.getName(), department
        );
        faculty.setDesignation(designation);
        faculty.setStatus("PENDING"); // Requires College Administrator approval before creating exams
        userAccountRepository.save(faculty);

        redirectAttributes.addFlashAttribute("successMessage",
                "Faculty application for '" + fullName + "' registered! Account is pending verification by the " + college.getName() + " Administrator.");
        return "redirect:/login";
    }

    // =========================================================================
    // 4. Password Change & Reset Workflows
    // =========================================================================
    @GetMapping("/change-password")
    public String showChangePasswordPage() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (authentication == null) {
            return "redirect:/login";
        }

        UserAccount user = userAccountRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Incorrect current password entered.");
            return "redirect:/change-password";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "New password must be at least 6 characters long.");
            return "redirect:/change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match.");
            return "redirect:/change-password";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userAccountRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully!");
        return "redirect:/profile";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "forgot-password";
    }
}
