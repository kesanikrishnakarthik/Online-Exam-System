package com.onlineexam.online_exam;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        String username = (authentication != null) ? authentication.getName() : "user";
        String role = "STUDENT";

        if (authentication != null) {
            boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isFaculty = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_FACULTY"));
            if (isAdmin) role = "COLLEGE ADMINISTRATOR";
            else if (isFaculty) role = "FACULTY";
            else role = "STUDENT";
        }

        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("college", "Apex Institute of Technology");
        model.addAttribute("department", "Computer Science & Engineering");
        model.addAttribute("studentId", "STU-2026-" + Math.abs(username.hashCode() % 9000 + 1000));
        model.addAttribute("status", "Active / Enrolled");

        return "profile";
    }
}
