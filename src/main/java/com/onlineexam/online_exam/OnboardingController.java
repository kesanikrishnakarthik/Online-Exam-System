package com.onlineexam.online_exam;

import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
    private final PasswordResetService passwordResetService;

    public OnboardingController(
            CollegeRepository collegeRepository,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            PasswordResetService passwordResetService) {
        this.collegeRepository = collegeRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetService = passwordResetService;
    }

    // =========================================================================
    // 1. College Onboarding with College Admin Account Generation
    // =========================================================================
    @GetMapping("/register-college")
    public String showCollegeRegistrationForm() {
        return "register-college";
    }

    @Transactional
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

        String trimmedName = (name != null) ? name.trim() : "";
        String trimmedCode = (code != null) ? code.trim().toUpperCase() : "";
        String trimmedAdminUser = (adminUsername != null) ? adminUsername.trim().toLowerCase() : "";

        if (trimmedName.isBlank() || trimmedCode.isBlank() || contactEmail.isBlank() || trimmedAdminUser.isBlank() || adminPassword.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "All required fields, including Administrator credentials, must be provided.");
            return "redirect:/register-college";
        }

        if (!adminPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Administrator passwords do not match.");
            return "redirect:/register-college";
        }

        if (collegeRepository.existsByCodeIgnoreCase(trimmedCode)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Institutional code '" + trimmedCode + "' is already registered in the platform.");
            return "redirect:/register-college";
        }

        if (userAccountRepository.existsByUsernameIgnoreCase(trimmedAdminUser)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Administrator username '" + trimmedAdminUser + "' is already taken. Please choose another username.");
            return "redirect:/register-college";
        }

        // Save new college tenant with status APPROVED
        College college = new College(trimmedName, trimmedCode, type, "APPROVED",
                address, contactPerson, contactEmail, contactPhone);
        collegeRepository.save(college);

        // Save linked College Administrator account with status ACTIVE
        UserAccount adminAccount = new UserAccount(
                trimmedAdminUser,
                passwordEncoder.encode(adminPassword),
                contactPerson,
                contactEmail,
                trimmedCode + "-ADM-01",
                "ROLE_ADMIN",
                trimmedCode,
                trimmedName,
                "Academic Administration"
        );
        adminAccount.setStatus("ACTIVE");
        userAccountRepository.save(adminAccount);

        redirectAttributes.addFlashAttribute("successMessage",
                "Institution '" + trimmedName + "' (Code: " + trimmedCode + ") registered successfully! Administrator account ('" + trimmedAdminUser + "') is now active. Please sign in below.");
        return "redirect:/login";
    }

    // =========================================================================
    // 2. Student Candidate Enrollment
    // =========================================================================
    @GetMapping("/register-student")
    public String showStudentRegistrationForm(Model model) {
        List<College> approvedColleges = collegeRepository.findByStatusIn(List.of("APPROVED", "ACTIVE"));
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

        String trimmedRoll = (rollNumber != null) ? rollNumber.trim() : "";

        if (collegeCode == null || collegeCode.isBlank() || fullName.isBlank() || trimmedRoll.isBlank() || email.isBlank() || password.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please complete all required fields.");
            return "redirect:/register-student";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match.");
            return "redirect:/register-student";
        }

        College college = null;
        String trimmedInput = collegeCode.trim();
        if (trimmedInput.matches("\\d+")) {
            college = collegeRepository.findById(Long.parseLong(trimmedInput)).orElse(null);
        }
        if (college == null) {
            college = collegeRepository.findByCodeIgnoreCase(trimmedInput).orElse(null);
        }

        if (college == null || (!"APPROVED".equalsIgnoreCase(college.getStatus()) && !"ACTIVE".equalsIgnoreCase(college.getStatus()))) {
            redirectAttributes.addFlashAttribute("errorMessage", "The selected college is not currently approved for examination registration.");
            return "redirect:/register-student";
        }

        String trimmedCode = college.getCode().trim().toUpperCase();

        if (userAccountRepository.existsByCollegeCodeIgnoreCaseAndInstitutionalIdIgnoreCase(trimmedCode, trimmedRoll)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Candidate with Roll Number '" + trimmedRoll + "' is already enrolled under " + college.getName() + ".");
            return "redirect:/register-student";
        }

        // Standardized globally-unique username: e.g. "ait_21b91a0501"
        String username = trimmedCode.toLowerCase() + "_" + trimmedRoll.toLowerCase();
        if (userAccountRepository.existsByUsernameIgnoreCase(username)) {
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
                "Enrollment successful! You can now log in using your Roll Number '" + trimmedRoll + "'.");
        return "redirect:/login";
    }

    // =========================================================================
    // 3. Faculty Onboarding & Controlled Verification
    // =========================================================================
    @GetMapping("/register-faculty")
    public String showFacultyRegistrationForm(Model model) {
        List<College> approvedColleges = collegeRepository.findByStatusIn(List.of("APPROVED", "ACTIVE"));
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

        String trimmedId = (facultyId != null) ? facultyId.trim() : "";

        if (collegeCode == null || collegeCode.isBlank() || fullName.isBlank() || trimmedId.isBlank() || email.isBlank() || password.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "All required faculty fields must be filled.");
            return "redirect:/register-faculty";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match.");
            return "redirect:/register-faculty";
        }

        College college = null;
        String trimmedInput = collegeCode.trim();
        if (trimmedInput.matches("\\d+")) {
            college = collegeRepository.findById(Long.parseLong(trimmedInput)).orElse(null);
        }
        if (college == null) {
            college = collegeRepository.findByCodeIgnoreCase(trimmedInput).orElse(null);
        }

        if (college == null || (!"APPROVED".equalsIgnoreCase(college.getStatus()) && !"ACTIVE".equalsIgnoreCase(college.getStatus()))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Selected institution is not approved or active.");
            return "redirect:/register-faculty";
        }

        String trimmedCode = college.getCode().trim().toUpperCase();

        if (userAccountRepository.existsByCollegeCodeIgnoreCaseAndInstitutionalIdIgnoreCase(trimmedCode, trimmedId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Faculty ID '" + trimmedId + "' is already registered in " + college.getName() + ".");
            return "redirect:/register-faculty";
        }

        String username = trimmedCode.toLowerCase() + "_" + trimmedId.toLowerCase();
        if (userAccountRepository.existsByUsernameIgnoreCase(username)) {
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

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam String identifier,
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {

        String trimmedId = (identifier != null) ? identifier.trim() : "";
        String trimmedEmail = (email != null) ? email.trim() : "";

        // Standard message for security (prevents user account enumeration)
        String genericMessage = "If an account matching the provided identifier and email exists, password reset instructions have been issued.";

        if (!trimmedId.isBlank() && !trimmedEmail.isBlank()) {
            Optional<UserAccount> userOpt = userAccountRepository.findByUsername(trimmedId);
            if (userOpt.isEmpty()) {
                List<UserAccount> byInstId = userAccountRepository.findAllByInstitutionalIdIgnoreCase(trimmedId);
                if (byInstId.size() == 1) {
                    userOpt = Optional.of(byInstId.get(0));
                } else if (byInstId.size() > 1) {
                    userOpt = byInstId.stream()
                            .filter(u -> trimmedEmail.equalsIgnoreCase(u.getEmail()))
                            .findFirst();
                }
            }

            if (userOpt.isPresent()) {
                UserAccount account = userOpt.get();
                if (trimmedEmail.equalsIgnoreCase(account.getEmail())) {
                    String rawToken = passwordResetService.createResetToken(account.getUsername(), account.getEmail());
                    // Expose development reset testing link when SMTP is not configured
                    redirectAttributes.addFlashAttribute("devResetToken", rawToken);
                    redirectAttributes.addFlashAttribute("devResetLink", "/reset-password?token=" + rawToken);
                }
            }
        }

        redirectAttributes.addFlashAttribute("infoMessage", genericMessage);
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam(required = false) String token, Model model) {
        if (token == null || passwordResetService.validateToken(token).isEmpty()) {
            model.addAttribute("errorMessage", "Password reset link is invalid, expired, or has already been used.");
            return "reset-password";
        }

        model.addAttribute("token", token.trim());
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(
            @RequestParam String token,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        if (token == null || passwordResetService.validateToken(token).isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Reset link is invalid or expired. Please submit a new request.");
            return "redirect:/forgot-password";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 6 characters long.");
            return "redirect:/reset-password?token=" + token;
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match.");
            return "redirect:/reset-password?token=" + token;
        }

        boolean success = passwordResetService.resetPassword(token, newPassword);
        if (!success) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to reset password. Token may have expired.");
            return "redirect:/forgot-password";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Password has been successfully updated! You can now sign in with your new password.");
        return "redirect:/login";
    }
}
