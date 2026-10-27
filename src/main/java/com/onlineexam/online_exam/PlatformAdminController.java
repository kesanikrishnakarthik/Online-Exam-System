package com.onlineexam.online_exam;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PlatformAdminController {

    private final CollegeRepository collegeRepository;
    private final UserAccountRepository userAccountRepository;
    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;

    public PlatformAdminController(
            CollegeRepository collegeRepository,
            UserAccountRepository userAccountRepository,
            ExamRepository examRepository,
            ExamResultRepository examResultRepository) {
        this.collegeRepository = collegeRepository;
        this.userAccountRepository = userAccountRepository;
        this.examRepository = examRepository;
        this.examResultRepository = examResultRepository;
    }

    @GetMapping("/platform/dashboard")
    public String platformDashboard(Model model) {
        List<College> allColleges = collegeRepository.findAll();
        long approvedCount = allColleges.stream().filter(c -> "APPROVED".equalsIgnoreCase(c.getStatus())).count();
        long pendingCount = allColleges.stream().filter(c -> "PENDING".equalsIgnoreCase(c.getStatus())).count();
        long totalUsers = userAccountRepository.count();
        long totalExams = examRepository.count();
        long totalResults = examResultRepository.count();

        model.addAttribute("colleges", allColleges);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalExams", totalExams);
        model.addAttribute("totalResults", totalResults);

        return "platform-dashboard";
    }

    @PostMapping("/platform/college/{id}/approve")
    public String approveCollege(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        College college = collegeRepository.findById(id).orElse(null);
        if (college != null) {
            college.setStatus("APPROVED");
            collegeRepository.save(college);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Institution '" + college.getName() + "' (Code: " + college.getCode() + ") has been approved!");
        }
        return "redirect:/platform/dashboard";
    }

    @PostMapping("/platform/college/{id}/reject")
    public String rejectCollege(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        College college = collegeRepository.findById(id).orElse(null);
        if (college != null) {
            college.setStatus("REJECTED");
            collegeRepository.save(college);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Institution '" + college.getName() + "' has been rejected.");
        }
        return "redirect:/platform/dashboard";
    }

    @PostMapping("/platform/college/{id}/suspend")
    public String suspendCollege(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        College college = collegeRepository.findById(id).orElse(null);
        if (college != null) {
            college.setStatus("SUSPENDED");
            collegeRepository.save(college);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Institution '" + college.getName() + "' has been suspended.");
        }
        return "redirect:/platform/dashboard";
    }
}

