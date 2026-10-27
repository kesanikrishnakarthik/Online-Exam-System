package com.onlineexam.online_exam;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FacultyController {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamResultRepository examResultRepository;
    private final ExamAttemptRepository examAttemptRepository;

    public FacultyController(
            ExamRepository examRepository,
            QuestionRepository questionRepository,
            ExamResultRepository examResultRepository,
            ExamAttemptRepository examAttemptRepository) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.examResultRepository = examResultRepository;
        this.examAttemptRepository = examAttemptRepository;
    }

    @GetMapping("/faculty/dashboard")
    public String facultyDashboard(Model model) {
        long totalExams = examRepository.count();
        long totalQuestions = questionRepository.count();
        long totalResults = examResultRepository.count();
        long totalAttempts = examAttemptRepository.count();

        model.addAttribute("totalExams", totalExams);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("totalResults", totalResults);
        model.addAttribute("totalAttempts", totalAttempts);
        model.addAttribute("message", "Welcome to the Faculty Dashboard");

        return "faculty-dashboard";
    }

    @GetMapping("/faculty/exams")
    public String manageExams(Model model) {
        List<Exam> exams = examRepository.findAll();
        model.addAttribute("exams", exams);
        return "faculty-exams";
    }

    @GetMapping("/faculty/results")
    public String studentPerformance(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String student,
            Model model) {

        List<ExamResult> results = examResultRepository.findAll();

        if (subject != null && !subject.isBlank()) {
            results = results.stream()
                    .filter(r -> r.getSubject().equalsIgnoreCase(subject.trim()))
                    .toList();
        }

        if (student != null && !student.isBlank()) {
            results = results.stream()
                    .filter(r -> r.getStudentUsername().toLowerCase().contains(student.trim().toLowerCase()))
                    .toList();
        }

        model.addAttribute("results", results);
        model.addAttribute("selectedSubject", subject);
        model.addAttribute("selectedStudent", student);

        return "faculty-results";
    }

    @GetMapping("/faculty/results/export")
    public void exportResultsToCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"college-exam-results.csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("Result ID,Exam Title,Subject,Student Username,College,Score,Total Marks,Percentage,Attempt,Violations,Submission Reason,Date");

        List<ExamResult> results = examResultRepository.findAll();
        for (ExamResult r : results) {
            writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%.2f%%,%d,%d,\"%s\",\"%s\"%n",
                    r.getId(),
                    r.getExamTitle() != null ? r.getExamTitle().replace("\"", "\"\"") : "",
                    r.getSubject() != null ? r.getSubject().replace("\"", "\"\"") : "",
                    r.getStudentUsername(),
                    r.getCollege() != null ? r.getCollege().replace("\"", "\"\"") : "",
                    r.getScore(),
                    r.getTotalMarks(),
                    r.getPercentage(),
                    r.getAttemptNumber(),
                    r.getViolationCount(),
                    r.getSubmissionReason(),
                    r.getSubmissionTime() != null ? r.getSubmissionTime().toString() : ""
            );
        }
    }
}
