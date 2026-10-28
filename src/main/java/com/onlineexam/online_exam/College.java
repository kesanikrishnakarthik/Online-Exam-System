package com.onlineexam.online_exam;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class College {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code; // Unique uppercase identifier, e.g. "AIT", "NIE", "MIT"

    private String type; // Engineering / B.Tech, Degree College, Polytechnic, University
    private String status = "APPROVED"; // APPROVED, ACTIVE, SUSPENDED
    private String address;
    private String contactPerson;
    private String contactEmail;
    private String contactPhone;
    private String departments = "Computer Science & Engineering (CSE), Computer Science (CS), CSE - Data Science (CSE-DS), Artificial Intelligence & Machine Learning (AIML), Artificial Intelligence & Data Science (AIDS), Information Technology (IT), Mechanical Engineering (MECH), Civil Engineering (CIVIL), Electronics & Communication Engineering (ECE), Electrical & Electronics Engineering (EEE)";
    private LocalDateTime createdAt = LocalDateTime.now();

    public College() {
    }

    public College(String name, String code, String type, String status, String address,
                   String contactPerson, String contactEmail, String contactPhone) {
        this.name = name;
        this.code = (code != null) ? code.trim().toUpperCase() : "";
        this.type = type;
        this.status = status;
        this.address = address;
        this.contactPerson = contactPerson;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = (code != null) ? code.trim().toUpperCase() : "";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getDepartments() {
        return departments;
    }

    public void setDepartments(String departments) {
        this.departments = departments;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

