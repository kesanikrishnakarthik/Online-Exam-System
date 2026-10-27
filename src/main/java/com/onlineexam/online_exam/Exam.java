package com.onlineexam.online_exam;

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
    private int durationMinutes;
    private int totalMarks;
    private String collegeCode = "AIT";
    private String college = "Apex Institute of Technology";
    private String department = "Computer Science & Engineering";
    private String branchProgram = "B.Tech CSE";
    private String academicYear = "3rd Year";
    private String semester = "1st Semester";
    private int maxAttempts = 3;
    private int maxViolations = 3;
    private boolean published = true;
    private String lifecycleStatus = "PUBLISHED"; // DRAFT, PUBLISHED, CLOSED

    public Exam() {
    }

    public Exam(String title, String subject, int durationMinutes, int totalMarks) {
        this.title = title;
        this.subject = subject;
        this.durationMinutes = durationMinutes;
        this.totalMarks = totalMarks;
    }

    public Exam(String title, String subject, int durationMinutes, int totalMarks,
                String college, String department, int maxAttempts, int maxViolations) {
        this.title = title;
        this.subject = subject;
        this.durationMinutes = durationMinutes;
        this.totalMarks = totalMarks;
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
}
