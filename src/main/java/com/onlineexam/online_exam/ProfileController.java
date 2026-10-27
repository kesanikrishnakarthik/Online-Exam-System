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
        String username = (authentication != null) ? authentication.getName() : "user";
        UserAccount account = userAccountRepository.findByUsername(username).orElse(null);

        String role = "STUDENT";
        if (authentication != null) {
            boolean isSuperAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PLATFORM_ADMIN"));
            boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isFaculty = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_FACULTY"));
            if (isSuperAdmin) role = "PLATFORM SUPER ADMINISTRATOR";
            else if (isAdmin) role = "COLLEGE ADMINISTRATOR";
            else if (isFaculty) role = "FACULTY";
            else role = "STUDENT";
        }

        String fullName = (account != null && account.getFullName() != null) ? account.getFullName() : username;
        String email = (account != null && account.getEmail() != null) ? account.getEmail() : username + "@college.edu";
        String collegeName = (account != null && account.getCollegeName() != null) ? account.getCollegeName() : "Apex Institute of Technology";
        String collegeCode = (account != null && account.getCollegeCode() != null) ? account.getCollegeCode() : "AIT";
        String department = (account != null && account.getDepartment() != null) ? account.getDepartment() : "Computer Science & Engineering";
        String idOrRoll = (account != null && account.getRollNumberOrFacultyId() != null) ? account.getRollNumberOrFacultyId() : "21B91A0501";
        String status = (account != null && account.getStatus() != null) ? account.getStatus() : "ACTIVE";
        String degreeBranch = (account != null && account.getDegreeBranch() != null) ? account.getDegreeBranch() : "B.Tech CSE";
        String academicYear = (account != null && account.getAcademicYear() != null) ? account.getAcademicYear() : "3rd Year";
        String semester = (account != null && account.getSemester() != null) ? account.getSemester() : "1st Semester";

        model.addAttribute("username", username);
        model.addAttribute("fullName", fullName);
        model.addAttribute("email", email);
        model.addAttribute("role", role);
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
