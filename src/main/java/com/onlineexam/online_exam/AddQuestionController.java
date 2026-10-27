package com.onlineexam.online_exam;

import java.util.Arrays;
import java.util.List;
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
                .orElseThrow(() -> new IllegalArgumentException("Exam not found: " + examId));

        List<Question> questions = questionRepository.findByExamId(examId);

        model.addAttribute("exam", exam);
        model.addAttribute("questions", questions);
        model.addAttribute("totalQuestions", questions.size());

        return "add-question";
    }

    @PostMapping("/faculty/exam/{examId}/add-question")
    public String addQuestion(
            @PathVariable Long examId,
            @RequestParam(defaultValue = "SINGLE_CHOICE") String questionType,
            @RequestParam String questionText,
            @RequestParam(required = false, defaultValue = "") String optionA,
            @RequestParam(required = false, defaultValue = "") String optionB,
            @RequestParam(required = false, defaultValue = "") String optionC,
            @RequestParam(required = false, defaultValue = "") String optionD,
            @RequestParam(required = false, defaultValue = "") String correctAnswerSingle,
            @RequestParam(required = false) List<String> correctOptionsMulti,
            @RequestParam(required = false, defaultValue = "") String acceptedAnswersFill,
            @RequestParam(required = false, defaultValue = "ALL_OR_NOTHING") String markingScheme,
            @RequestParam(required = false, defaultValue = "false") boolean caseSensitive,
            @RequestParam(defaultValue = "4") int marks,
            @RequestParam(defaultValue = "0") int negativeMarks,
            RedirectAttributes redirectAttributes) {

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found: " + examId));

        if (questionText.isBlank() || marks < 1) {
            redirectAttributes.addFlashAttribute("errorMessage", "Question text and positive marks are required.");
            return "redirect:/faculty/exam/" + examId + "/add-question";
        }

        String computedCorrectAnswer = "";

        if ("SINGLE_CHOICE".equalsIgnoreCase(questionType)) {
            if (optionA.isBlank() || optionB.isBlank() || optionC.isBlank() || optionD.isBlank()) {
                redirectAttributes.addFlashAttribute("errorMessage", "All 4 options must be provided for single-choice questions.");
                return "redirect:/faculty/exam/" + examId + "/add-question";
            }
            if (correctAnswerSingle.isBlank() || !List.of("A", "B", "C", "D").contains(correctAnswerSingle.trim().toUpperCase())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please select one correct answer (A, B, C, or D).");
                return "redirect:/faculty/exam/" + examId + "/add-question";
            }
            computedCorrectAnswer = correctAnswerSingle.trim().toUpperCase();

        } else if ("MULTIPLE_SELECT".equalsIgnoreCase(questionType)) {
            if (optionA.isBlank() || optionB.isBlank() || optionC.isBlank() || optionD.isBlank()) {
                redirectAttributes.addFlashAttribute("errorMessage", "All 4 options must be provided for multiple-select questions.");
                return "redirect:/faculty/exam/" + examId + "/add-question";
            }
            if (correctOptionsMulti == null || correctOptionsMulti.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please select at least two correct options for multiple-select.");
                return "redirect:/faculty/exam/" + examId + "/add-question";
            }
            // Sort and join: e.g. "A,C"
            computedCorrectAnswer = String.join(",", correctOptionsMulti);

        } else if ("FILL_BLANKS".equalsIgnoreCase(questionType)) {
            if (acceptedAnswersFill.isBlank()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please provide at least one accepted answer for fill-in-the-blank.");
                return "redirect:/faculty/exam/" + examId + "/add-question";
            }
            // Normalize comma-separated accepted answers
            computedCorrectAnswer = String.join(",", Arrays.stream(acceptedAnswersFill.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList());
        }

        Question question = new Question(
                examId,
                questionText.trim(),
                questionType.toUpperCase(),
                optionA.trim(),
                optionB.trim(),
                optionC.trim(),
                optionD.trim(),
                computedCorrectAnswer,
                markingScheme,
                caseSensitive,
                marks,
                negativeMarks
        );

        questionRepository.save(question);

        // Update exam total marks automatically
        int updatedTotalMarks = questionRepository.findByExamId(examId).stream()
                .mapToInt(Question::getMarks)
                .sum();
        exam.setTotalMarks(updatedTotalMarks);
        examRepository.save(exam);

        redirectAttributes.addFlashAttribute("successMessage",
                "Question of type '" + questionType + "' added successfully!");
        return "redirect:/faculty/exam/" + examId + "/add-question";
    }

    @PostMapping("/faculty/exam/{examId}/question/{questionId}/delete")
    public String deleteQuestion(
            @PathVariable Long examId,
            @PathVariable Long questionId,
            RedirectAttributes redirectAttributes) {

        Exam exam = examRepository.findById(examId).orElse(null);
        Question question = questionRepository.findById(questionId).orElse(null);

        if (exam != null && question != null && examId.equals(question.getExamId())) {
            questionRepository.delete(question);

            int updatedTotalMarks = questionRepository.findByExamId(examId).stream()
                    .mapToInt(Question::getMarks)
                    .sum();
            exam.setTotalMarks(updatedTotalMarks);
            examRepository.save(exam);

            redirectAttributes.addFlashAttribute("successMessage", "Question deleted successfully.");
        }

        return "redirect:/faculty/exam/" + examId + "/add-question";
    }
}
