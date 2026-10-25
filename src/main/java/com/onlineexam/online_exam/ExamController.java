
package com.onlineexam.online_exam;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ExamController {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;

    public ExamController(
            ExamRepository examRepository,
            QuestionRepository questionRepository) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
    }

    @GetMapping("/exams")
    public String availableExams(Model model) {
        List<Exam> exams = examRepository.findAll();
        model.addAttribute("exams", exams);
        return "exams";
    }

    @GetMapping("/exam/{examId}")
    public String startExam(
            @PathVariable Long examId,
            Model model) {

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Exam not found"));

        List<Question> questions =
                questionRepository.findByExamId(examId);

        model.addAttribute("exam", exam);
        model.addAttribute("questions", questions);

        return "exam";
    }

    @PostMapping("/exam/{examId}/submit")
    public String submitExam(
            @PathVariable Long examId,
            @RequestParam Map<String, String> answers,
            Model model) {

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Exam not found"));

        List<Question> questions =
                questionRepository.findByExamId(examId);

        int score = 0;
        int totalMarks = 0;

        for (Question question : questions) {
            totalMarks += question.getMarks();

            String answer =
                    answers.get("question_" + question.getId());

            if (answer != null &&
                    answer.equalsIgnoreCase(
                            question.getCorrectAnswer())) {
                score += question.getMarks();
            }
        }

        model.addAttribute("exam", exam);
        model.addAttribute("score", score);
        model.addAttribute("totalMarks", totalMarks);

        return "exam-result";
    }
}

