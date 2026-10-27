package com.onlineexam.online_exam;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String fullName;
    private String email;
    private String institutionalId; // Student Roll Number or Faculty Employee ID
    private String role; // ROLE_STUDENT, ROLE_FACULTY, ROLE_ADMIN, ROLE_PLATFORM_ADMIN
    private String collegeCode;
    private String collegeName;
    private String department;
    private String degreeBranch;
    private String academicYear;
    private String semester;
    private String section;
    private String status = "ACTIVE"; // ACTIVE, PENDING_APPROVAL, SUSPENDED
    private LocalDateTime createdAt = LocalDateTime.now();

    public UserAccount() {
    }

    public UserAccount(String username, String password, String fullName, String email,
                       String institutionalId, String role, String collegeCode,
                       String collegeName, String department) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.institutionalId = institutionalId;
        this.role = role;
        this.collegeCode = (collegeCode != null) ? collegeCode.trim().toUpperCase() : "";
        this.collegeName = collegeName;
        this.department = department;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getInstitutionalId() {
        return institutionalId;
    }

    public void setInstitutionalId(String institutionalId) {
        this.institutionalId = institutionalId;
    }

    public String getRollNumberOrFacultyId() {
        return institutionalId;
    }

    public void setRollNumberOrFacultyId(String id) {
        this.institutionalId = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCollegeCode() {
        return collegeCode;
    }

    public void setCollegeCode(String collegeCode) {
        this.collegeCode = (collegeCode != null) ? collegeCode.trim().toUpperCase() : "";
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDegreeBranch() {
        return degreeBranch;
    }

    public void setDegreeBranch(String degreeBranch) {
        this.degreeBranch = degreeBranch;
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

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
