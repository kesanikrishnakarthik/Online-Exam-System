package com.onlineexam.online_exam;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ExamResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long examId;
    private String examTitle;
    private String subject;
    private String studentUsername;
    private String college = "Apex Institute of Technology";
    private int score;
    private int totalMarks;
    private int attemptNumber = 1;
    private LocalDateTime submissionTime = LocalDateTime.now();
    private int violationCount = 0;
    private String submissionReason = "NORMAL"; // NORMAL, VIOLATION_LIMIT, TIMEOUT

    public ExamResult() {
    }

    public ExamResult(Long examId, String examTitle, String subject,
                      String studentUsername, int score, int totalMarks) {
        this.examId = examId;
        this.examTitle = examTitle;
        this.subject = subject;
        this.studentUsername = studentUsername;
        this.score = score;
        this.totalMarks = totalMarks;
        this.submissionTime = LocalDateTime.now();
        this.attemptNumber = 1;
        this.submissionReason = "NORMAL";
    }

    public ExamResult(Long examId, String examTitle, String subject,
                      String studentUsername, String college, int score,
                      int totalMarks, int attemptNumber, int violationCount,
                      String submissionReason) {
        this.examId = examId;
        this.examTitle = examTitle;
        this.subject = subject;
        this.studentUsername = studentUsername;
        this.college = college;
        this.score = score;
        this.totalMarks = totalMarks;
        this.attemptNumber = attemptNumber;
        this.violationCount = violationCount;
        this.submissionReason = submissionReason;
        this.submissionTime = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public void setStudentUsername(String studentUsername) {
        this.studentUsername = studentUsername;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public LocalDateTime getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(LocalDateTime submissionTime) {
        this.submissionTime = submissionTime;
    }

    public int getViolationCount() {
        return violationCount;
    }

    public void setViolationCount(int violationCount) {
        this.violationCount = violationCount;
    }

    public String getSubmissionReason() {
        return submissionReason;
    }

    public void setSubmissionReason(String submissionReason) {
        this.submissionReason = submissionReason;
    }

    public double getPercentage() {
        if (totalMarks == 0) return 0.0;
        return Math.round((score * 100.0 / totalMarks) * 100.0) / 100.0;
    }
}
