package com.onlineexam.online_exam;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ResultsController {


private final ExamResultRepository examResultRepository;

public ResultsController(ExamResultRepository examResultRepository) {
    this.examResultRepository = examResultRepository;
}

@GetMapping("/results")
public String results(Model model, Authentication authentication) {

    String studentUsername = authentication.getName();

    List<ExamResult> results = examResultRepository.findAll()
            .stream()
            .filter(result -> result.getStudentUsername()
                    .equals(studentUsername))
            .toList();

    model.addAttribute("results", results);

    return "results";
}


}
