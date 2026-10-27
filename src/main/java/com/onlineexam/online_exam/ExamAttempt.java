package com.onlineexam.online_exam;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ExamAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long examId;
    private String examTitle;
    private String subject;
    private String studentUsername;
    private String college = "Apex Institute of Technology";
    private int attemptNumber;
    private LocalDateTime startTime;
    private LocalDateTime submissionTime;
    private int score;
    private int totalMarks;
    private int violationCount;
    private String status; // IN_PROGRESS, COMPLETED, AUTO_SUBMITTED_VIOLATION, AUTO_SUBMITTED_TIMEOUT
    private String submissionReason;

    public ExamAttempt() {
    }

    public ExamAttempt(Long examId, String examTitle, String subject,
                       String studentUsername, String college, int attemptNumber,
                       LocalDateTime startTime, String status) {
        this.examId = examId;
        this.examTitle = examTitle;
        this.subject = subject;
        this.studentUsername = studentUsername;
        this.college = college;
        this.attemptNumber = attemptNumber;
        this.startTime = startTime;
        this.status = status;
        this.violationCount = 0;
        this.score = 0;
        this.totalMarks = 0;
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

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(LocalDateTime submissionTime) {
        this.submissionTime = submissionTime;
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

    public int getViolationCount() {
        return violationCount;
    }

    public void setViolationCount(int violationCount) {
        this.violationCount = violationCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

