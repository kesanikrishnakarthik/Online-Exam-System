package com.onlineexam.online_exam;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AddQuestionController {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;

    public AddQuestionController(
            ExamRepository examRepository,
            QuestionRepository questionRepository) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
    }

    @GetMapping("/faculty/exam/{examId}/add-question")
    public String showAddQuestionPage(
            @PathVariable Long examId,
            Model model) {

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found"));

        model.addAttribute("exam", exam);
        model.addAttribute("questions", questionRepository.findByExamId(examId));

        return "add-question";
    }

    @PostMapping("/faculty/exam/{examId}/add-question")
    public String addQuestion(
            @PathVariable Long examId,
            @RequestParam String questionText,
            @RequestParam String optionA,
            @RequestParam String optionB,
            @RequestParam String optionC,
            @RequestParam String optionD,
            @RequestParam String correctAnswer,
            @RequestParam int marks,
            RedirectAttributes redirectAttributes) {

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found"));

        if (questionText.isBlank()
                || optionA.isBlank()
                || optionB.isBlank()
                || optionC.isBlank()
                || optionD.isBlank()
                || !(correctAnswer.equals("A")
                    || correctAnswer.equals("B")
                    || correctAnswer.equals("C")
                    || correctAnswer.equals("D"))
                || marks < 1) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Enter valid question details and marks.");

            return "redirect:/faculty/exam/" + examId + "/add-question";
        }

        Question question = new Question(
                examId,
                questionText,
                optionA,
                optionB,
                optionC,
                optionD,
                correctAnswer,
                marks);

        questionRepository.save(question);

        // Update exam total marks automatically
        int updatedTotalMarks = questionRepository.findByExamId(examId).stream()
                .mapToInt(Question::getMarks)
                .sum();
        exam.setTotalMarks(updatedTotalMarks);
        examRepository.save(exam);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Question added successfully!");

        return "redirect:/faculty/exam/" + examId + "/add-question";
    }
}
