
package com.onlineexam.online_exam;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ResultsController {

    @GetMapping("/results")
    public String results(Model model) {
        model.addAttribute("message", "You have not completed any exams yet.");
        return "results";
    }
}

