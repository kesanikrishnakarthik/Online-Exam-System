package com.onlineexam.online_exam;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CreateExamController {

    private final ExamRepository examRepository;

    public CreateExamController(ExamRepository examRepository) {
        this.examRepository = examRepository;
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
            RedirectAttributes redirectAttributes) {

        if (title.isBlank() || subject.isBlank()
                || durationMinutes < 1 || durationMinutes > 300) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Enter valid exam details.");
            return "redirect:/faculty/create-exam";
        }

        Exam exam = new Exam(title, subject, durationMinutes, 0);
        exam.setMaxAttempts(maxAttempts > 0 ? maxAttempts : 3);
        exam.setMaxViolations(maxViolations > 0 ? maxViolations : 3);
        examRepository.save(exam);

        return "redirect:/faculty/exam/" + exam.getId() + "/add-question";
    }
}
