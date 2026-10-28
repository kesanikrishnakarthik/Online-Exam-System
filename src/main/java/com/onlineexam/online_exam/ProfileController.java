package com.onlineexam.online_exam;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    private final UserAccountRepository userAccountRepository;

    public ProfileController(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        String username = (authentication != null) ? authentication.getName() : "";
        UserAccount account = userAccountRepository.findByUsername(username).orElse(null);

        String role = "STUDENT";
        String dashboardUrl = "/dashboard";

        if (authentication != null) {
            boolean isSuperAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));
            boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_COLLEGE_ADMIN"));
            boolean isFaculty = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_FACULTY"));

            if (isSuperAdmin) {
                role = "PLATFORM SUPER ADMINISTRATOR";
                dashboardUrl = "/platform/dashboard";
            } else if (isAdmin) {
                role = "COLLEGE ADMINISTRATOR";
                dashboardUrl = "/admin/dashboard";
            } else if (isFaculty) {
                role = "FACULTY";
                dashboardUrl = "/faculty/dashboard";
            } else {
                role = "STUDENT";
                dashboardUrl = "/dashboard";
            }
        }

        String fullName = (account != null && account.getFullName() != null) ? account.getFullName() : username;
        String email = (account != null && account.getEmail() != null) ? account.getEmail() : "";
        String collegeName = (account != null && account.getCollegeName() != null) ? account.getCollegeName() : "Institutional Examination Portal";
        String collegeCode = (account != null && account.getCollegeCode() != null) ? account.getCollegeCode() : "";
        String department = (account != null && account.getDepartment() != null) ? account.getDepartment() : "General";
        String idOrRoll = (account != null && account.getRollNumberOrFacultyId() != null) ? account.getRollNumberOrFacultyId() : username;
        String status = (account != null && account.getStatus() != null) ? account.getStatus() : "ACTIVE";
        String degreeBranch = (account != null && account.getDegreeBranch() != null) ? account.getDegreeBranch() : "";
        String academicYear = (account != null && account.getAcademicYear() != null) ? account.getAcademicYear() : "";
        String semester = (account != null && account.getSemester() != null) ? account.getSemester() : "";

        model.addAttribute("username", username);
        model.addAttribute("fullName", fullName);
        model.addAttribute("email", email);
        model.addAttribute("role", role);
        model.addAttribute("dashboardUrl", dashboardUrl);
        model.addAttribute("college", collegeName);
        model.addAttribute("collegeCode", collegeCode);
        model.addAttribute("department", department);
        model.addAttribute("studentId", idOrRoll);
        model.addAttribute("status", status);
        model.addAttribute("degreeBranch", degreeBranch);
        model.addAttribute("academicYear", academicYear);
        model.addAttribute("semester", semester);

        return "profile";
    }
}
