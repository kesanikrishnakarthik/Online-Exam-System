package com.onlineexam.online_exam;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner loadData(
            CollegeRepository collegeRepository,
            UserAccountRepository userAccountRepository,
            ExamRepository examRepository,
            QuestionRepository questionRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // 1. Seed Multiple Colleges
            if (collegeRepository.count() == 0) {
                collegeRepository.save(new College(
                        "Apex Institute of Technology", "AIT", "Engineering / B.Tech",
                        "APPROVED", "Tech Park Road, Hyderabad, Telangana",
                        "Dr. R. Ramanujan", "dean@ait.edu", "+91-40-23456789"
                ));

                collegeRepository.save(new College(
                        "National Institute of Engineering", "NIE", "Engineering / B.Tech",
                        "APPROVED", "Outer Ring Road, Bengaluru, Karnataka",
                        "Dr. S. K. Murthy", "principal@nie.edu", "+91-80-98765432"
                ));

                collegeRepository.save(new College(
                        "St. Xavier's Degree College", "SXC", "Degree College",
                        "PENDING", "Marine Lines, Mumbai, Maharashtra",
                        "Prof. M. D'Souza", "admin@sxc.edu", "+91-22-11223344"
                ));
            }

            // 2. Seed User Accounts (Preserving demo users + adding multi-college & superadmin)
            if (userAccountRepository.count() == 0) {
                // Platform Super Administrator
                userAccountRepository.save(new UserAccount(
                        "superadmin", passwordEncoder.encode("Super@123"),
                        "Global Platform Administrator", "superadmin@collegeconnect.edu",
                        "PLAT-001", "ROLE_PLATFORM_ADMIN", "GLOBAL", "CollegeConnect Platform", "Administration"
                ));

                // AIT - College Administrator
                userAccountRepository.save(new UserAccount(
                        "admin", passwordEncoder.encode("Admin@123"),
                        "Dean of Examinations (AIT)", "admin@ait.edu",
                        "AIT-ADM-01", "ROLE_ADMIN", "AIT", "Apex Institute of Technology", "Academic Administration"
                ));

                // AIT - Faculty
                userAccountRepository.save(new UserAccount(
                        "faculty", passwordEncoder.encode("Faculty@123"),
                        "Prof. K. Sharma", "faculty@ait.edu",
                        "FAC-CSE-101", "ROLE_FACULTY", "AIT", "Apex Institute of Technology", "Computer Science & Engineering"
                ));

                // AIT - Student (Roll Number: 21B91A0501)
                UserAccount aitStudent = new UserAccount(
                        "user", passwordEncoder.encode("Exam@123"),
                        "Karthik Kesani", "karthik@ait.edu",
                        "21B91A0501", "ROLE_STUDENT", "AIT", "Apex Institute of Technology", "Computer Science & Engineering"
                );
                aitStudent.setDegreeBranch("B.Tech - CSE");
                aitStudent.setAcademicYear("3rd Year");
                aitStudent.setSemester("1st Semester");
                aitStudent.setSection("A");
                userAccountRepository.save(aitStudent);

                // NIE - Faculty
                userAccountRepository.save(new UserAccount(
                        "nie_faculty", passwordEncoder.encode("Faculty@123"),
                        "Dr. Ramesh Babu", "faculty@nie.edu",
                        "NIE-FAC-204", "ROLE_FACULTY", "NIE", "National Institute of Engineering", "Information Technology"
                ));

                // NIE - Student
                UserAccount nieStudent = new UserAccount(
                        "nie_student", passwordEncoder.encode("Exam@123"),
                        "Ananya Rao", "ananya@nie.edu",
                        "NIE-2026-001", "ROLE_STUDENT", "NIE", "National Institute of Engineering", "Information Technology"
                );
                nieStudent.setDegreeBranch("B.Tech - IT");
                nieStudent.setAcademicYear("2nd Year");
                nieStudent.setSemester("2nd Semester");
                nieStudent.setSection("B");
                userAccountRepository.save(nieStudent);
            }

            // 3. Seed Multi-College Examinations
            if (examRepository.count() == 0) {
                // AIT Exams
                Exam aitExam1 = new Exam("Java Programming", "Java", 30, 20);
                aitExam1.setCollegeCode("AIT");
                aitExam1.setCollege("Apex Institute of Technology");
                aitExam1.setDepartment("Computer Science & Engineering");
                aitExam1.setBranchProgram("B.Tech CSE");
                examRepository.save(aitExam1);

                Exam aitExam2 = new Exam("Database Management Systems", "DBMS", 30, 20);
                aitExam2.setCollegeCode("AIT");
                aitExam2.setCollege("Apex Institute of Technology");
                aitExam2.setDepartment("Computer Science & Engineering");
                aitExam2.setBranchProgram("B.Tech CSE");
                examRepository.save(aitExam2);

                Exam aitExam3 = new Exam("Computer Networks", "CN", 45, 30);
                aitExam3.setCollegeCode("AIT");
                aitExam3.setCollege("Apex Institute of Technology");
                aitExam3.setDepartment("Computer Science & Engineering");
                aitExam3.setBranchProgram("B.Tech CSE");
                examRepository.save(aitExam3);

                // NIE Exams
                Exam nieExam1 = new Exam("Python for Data Science", "Python", 35, 20);
                nieExam1.setCollegeCode("NIE");
                nieExam1.setCollege("National Institute of Engineering");
                nieExam1.setDepartment("Information Technology");
                nieExam1.setBranchProgram("B.Tech IT");
                examRepository.save(nieExam1);
            }

            // 4. Seed Questions (Single-Choice, Multiple-Select, Fill-in-the-Blanks)
            if (questionRepository.count() == 0) {
                Exam javaExam = examRepository.findAll()
                        .stream()
                        .filter(e -> "AIT".equalsIgnoreCase(e.getCollegeCode()) && "Java".equalsIgnoreCase(e.getSubject()))
                        .findFirst()
                        .orElse(null);

                if (javaExam != null) {
                    // Type 1: Single Choice MCQ
                    questionRepository.save(new Question(
                            javaExam.getId(), "Which keyword is used to inherit a class in Java?",
                            "implements", "extends", "inherits", "super", "B", 4));

                    questionRepository.save(new Question(
                            javaExam.getId(), "Which method is the entry point of a Java application?",
                            "start()", "run()", "main()", "init()", "C", 4));

                    questionRepository.save(new Question(
                            javaExam.getId(), "Which of these is not a primitive data type in Java?",
                            "int", "char", "String", "boolean", "C", 4));

                    questionRepository.save(new Question(
                            javaExam.getId(), "Which keyword is used to create an object in Java?",
                            "class", "new", "this", "void", "B", 4));

                    questionRepository.save(new Question(
                            javaExam.getId(), "Which collection does not allow duplicate elements?",
                            "List", "ArrayList", "Set", "Queue", "C", 4));

                    // Type 2: Multiple-Select (All-or-Nothing)
                    questionRepository.save(new Question(
                            javaExam.getId(),
                            "Which of the following are valid access modifiers in Java? (Select all that apply)",
                            "MULTIPLE_SELECT",
                            "public", "private", "protected", "friend",
                            "A,B,C", "ALL_OR_NOTHING", false, 4, 0));

                    // Type 2: Multiple-Select (Partial Credit)
                    questionRepository.save(new Question(
                            javaExam.getId(),
                            "Which of the following classes implement the java.util.List interface? (Select all that apply)",
                            "MULTIPLE_SELECT",
                            "ArrayList", "LinkedList", "Vector", "HashSet",
                            "A,B,C", "PARTIAL_CREDIT", false, 4, 0));

                    // Type 3: Fill-in-the-Blanks (Accepts: metaspace, method area, permgen)
                    questionRepository.save(new Question(
                            javaExam.getId(),
                            "What is the name of the memory area in the JVM where class metadata and bytecode are loaded?",
                            "FILL_BLANKS",
                            "", "", "", "",
                            "metaspace, method area, permgen", "ALL_OR_NOTHING", false, 4, 0));

                    // Type 3: Fill-in-the-Blanks (final)
                    questionRepository.save(new Question(
                            javaExam.getId(),
                            "Which Java keyword is used to prevent a class from being inherited or a method from being overridden?",
                            "FILL_BLANKS",
                            "", "", "", "",
                            "final", "ALL_OR_NOTHING", false, 4, 0));

                    javaExam.setTotalMarks(36);
                    javaExam.setPassMarks(15);
                    examRepository.save(javaExam);
                }

                Exam pythonExam = examRepository.findAll()
                        .stream()
                        .filter(e -> "NIE".equalsIgnoreCase(e.getCollegeCode()))
                        .findFirst()
                        .orElse(null);

                if (pythonExam != null) {
                    questionRepository.save(new Question(
                            pythonExam.getId(), "Which data structure is immutable in Python?",
                            "List", "Dictionary", "Tuple", "Set", "C", 5));

                    questionRepository.save(new Question(
                            pythonExam.getId(), "What is the output of type([]) in Python?",
                            "<class 'tuple'>", "<class 'list'>", "<class 'dict'>", "<class 'array'>", "B", 5));

                    questionRepository.save(new Question(
                            pythonExam.getId(),
                            "Which of the following are built-in collection types in Python? (Select all that apply)",
                            "MULTIPLE_SELECT",
                            "list", "set", "tuple", "arraylist",
                            "A,B,C", "ALL_OR_NOTHING", false, 5, 0));

                    questionRepository.save(new Question(
                            pythonExam.getId(),
                            "Which keyword is used to define an anonymous or inline function in Python?",
                            "FILL_BLANKS",
                            "", "", "", "",
                            "lambda", "ALL_OR_NOTHING", false, 5, 0));

                    pythonExam.setTotalMarks(20);
                    pythonExam.setPassMarks(8);
                    examRepository.save(pythonExam);
                }
            }
        };
    }
}
