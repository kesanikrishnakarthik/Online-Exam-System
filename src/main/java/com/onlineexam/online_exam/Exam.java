package com.onlineexam.online_exam;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String subject;
    private String subjectCode = "CS301";

    @Column(length = 2000)
    private String instructions = "1. Answer all questions.\n2. Do not switch tabs or minimize the browser window.\n3. Automatic submission triggers upon 3 violations or when the timer hits 00:00.";

    private int durationMinutes;
    private int totalMarks;
    private int passMarks = 8; // Default passing threshold
    private boolean negativeMarking = false;

    private String collegeCode;
    private String college;
    private String department;
    private String branchProgram;
    private String academicYear;
    private String semester;
    private int maxAttempts = 3;
    private int maxViolations = 3;
    private boolean published = true;
    private String lifecycleStatus = "PUBLISHED"; // DRAFT, PUBLISHED, CLOSED
    private String createdBy;

    public Exam() {
    }

    public Exam(String title, String subject, int durationMinutes, int totalMarks) {
        this.title = title;
        this.subject = subject;
        this.durationMinutes = durationMinutes;
        this.totalMarks = totalMarks;
        this.passMarks = (int) Math.round(totalMarks * 0.40);
    }

    public Exam(String title, String subject, int durationMinutes, int totalMarks,
                String college, String department, int maxAttempts, int maxViolations) {
        this.title = title;
        this.subject = subject;
        this.durationMinutes = durationMinutes;
        this.totalMarks = totalMarks;
        this.passMarks = (int) Math.round(totalMarks * 0.40);
        this.college = college;
        this.department = department;
        this.maxAttempts = maxAttempts;
        this.maxViolations = maxViolations;
        this.published = true;
        this.lifecycleStatus = "PUBLISHED";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
        if (this.passMarks == 0 && totalMarks > 0) {
            this.passMarks = (int) Math.round(totalMarks * 0.40);
        }
    }

    public int getPassMarks() {
        return passMarks > 0 ? passMarks : Math.max(1, (int) Math.round(totalMarks * 0.40));
    }

    public void setPassMarks(int passMarks) {
        this.passMarks = passMarks;
    }

    public boolean isNegativeMarking() {
        return negativeMarking;
    }

    public void setNegativeMarking(boolean negativeMarking) {
        this.negativeMarking = negativeMarking;
    }

    public String getCollegeCode() {
        return collegeCode;
    }

    public void setCollegeCode(String collegeCode) {
        this.collegeCode = (collegeCode != null) ? collegeCode.trim().toUpperCase() : "";
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getBranchProgram() {
        return branchProgram;
    }

    public void setBranchProgram(String branchProgram) {
        this.branchProgram = branchProgram;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int getMaxViolations() {
        return maxViolations;
    }

    public void setMaxViolations(int maxViolations) {
        this.maxViolations = maxViolations;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public String getLifecycleStatus() {
        return lifecycleStatus;
    }

    public void setLifecycleStatus(String lifecycleStatus) {
        this.lifecycleStatus = lifecycleStatus;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
