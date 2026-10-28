package com.onlineexam.online_exam;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Value("${app.seed.test-data:false}")
    private boolean seedTestData;

    @Value("${app.clean-demo-data:true}")
    private boolean cleanDemoData;

    @Value("${SUPERADMIN_USERNAME:${app.security.superadmin-username:superadmin}}")
    private String superadminUsername;

    @Value("${SUPERADMIN_PASSWORD:${app.security.superadmin-password:}}")
    private String superadminPassword;

    @Value("${SUPERADMIN_EMAIL:${app.security.superadmin-email:superadmin@collegeconnect.edu}}")
    private String superadminEmail;

    @Bean
    CommandLineRunner loadData(
            CollegeRepository collegeRepository,
            UserAccountRepository userAccountRepository,
            ExamRepository examRepository,
            QuestionRepository questionRepository,
            ExamAttemptRepository examAttemptRepository,
            ExamResultRepository examResultRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // =========================================================================
            // 1. Safe Cleanup Procedure: Purge Legacy Demonstration Data
            // =========================================================================
            if (cleanDemoData && !seedTestData) {
                Set<String> demoColleges = Set.of("AIT", "NIE", "SXC");
                Set<String> demoUsernames = Set.of("admin", "faculty", "user", "nie_faculty", "nie_student");

                // Purge results and attempts linked to demo colleges or demo usernames
                List<ExamResult> demoResults = examResultRepository.findAll().stream()
                        .filter(r -> demoColleges.contains(r.getCollegeCode()) || demoUsernames.contains(r.getStudentUsername()))
                        .toList();
                if (!demoResults.isEmpty()) {
                    examResultRepository.deleteAll(demoResults);
                }

                List<ExamAttempt> demoAttempts = examAttemptRepository.findAll().stream()
                        .filter(a -> demoColleges.contains(a.getCollegeCode()) || demoUsernames.contains(a.getStudentUsername()))
                        .toList();
                if (!demoAttempts.isEmpty()) {
                    examAttemptRepository.deleteAll(demoAttempts);
                }

                // Purge demo exams and their questions
                List<Exam> demoExams = examRepository.findAll().stream()
                        .filter(e -> demoColleges.contains(e.getCollegeCode()))
                        .toList();
                for (Exam ex : demoExams) {
                    List<Question> qs = questionRepository.findByExamId(ex.getId());
                    if (!qs.isEmpty()) {
                        questionRepository.deleteAll(qs);
                    }
                }
                if (!demoExams.isEmpty()) {
                    examRepository.deleteAll(demoExams);
                }

                // Purge demo user accounts
                List<UserAccount> demoUsers = userAccountRepository.findAll().stream()
                        .filter(u -> demoUsernames.contains(u.getUsername()) || demoColleges.contains(u.getCollegeCode()))
                        .toList();
                if (!demoUsers.isEmpty()) {
                    userAccountRepository.deleteAll(demoUsers);
                }

                // Purge demo colleges
                List<College> demoCollegesList = collegeRepository.findAll().stream()
                        .filter(c -> demoColleges.contains(c.getCode()))
                        .toList();
                if (!demoCollegesList.isEmpty()) {
                    collegeRepository.deleteAll(demoCollegesList);
                }

                log.info("Safe demonstration data purge complete. Database reflects only genuine registrations.");
            }

            // =========================================================================
            // 2. Initial Super-Administrator Provisioning (Without hardcoded public password)
            // =========================================================================
            List<UserAccount> existingAdmins = userAccountRepository.findByRole("ROLE_PLATFORM_ADMIN");
            if (existingAdmins.isEmpty()) {
                String effectivePassword;
                boolean generated = false;

                if (superadminPassword != null && !superadminPassword.isBlank()) {
                    effectivePassword = superadminPassword.trim();
                } else {
                    // Generate a 16-character cryptographically secure password
                    byte[] randomBytes = new byte[8];
                    new SecureRandom().nextBytes(randomBytes);
                    effectivePassword = HexFormat.of().formatHex(randomBytes) + "A1!";
                    generated = true;
                }

                UserAccount superAdmin = new UserAccount(
                        superadminUsername.trim(),
                        passwordEncoder.encode(effectivePassword),
                        "Central Platform Super Administrator",
                        superadminEmail.trim(),
                        "PLAT-001",
                        "ROLE_PLATFORM_ADMIN",
                        "GLOBAL",
                        "CollegeConnect Central Administration",
                        "System Architecture & Governance"
                );
                superAdmin.setStatus("ACTIVE");
                userAccountRepository.save(superAdmin);

                System.out.println("================================================================================");
                System.out.println("[SECURITY PROVISIONING] Initial Platform Super-Administrator Created Successfully");
                System.out.println("Username: " + superadminUsername.trim());
                if (generated) {
                    System.out.println("Temporary Generated Password: " + effectivePassword);
                    System.out.println("ACTION REQUIRED: Store this credential and update password upon initial login.");
                } else {
                    System.out.println("Password configured via environment / application configuration.");
                }
                System.out.println("================================================================================");
            }

            // =========================================================================
            // 3. Optional Test Data Seeding (Disabled by default; activated via app.seed.test-data=true)
            // =========================================================================
            if (seedTestData && collegeRepository.count() == 0) {
                log.info("Seeding optional test dataset because app.seed.test-data=true...");
                // Test-only seeding code can execute here if explicitly enabled
            }
        };
    }
}
