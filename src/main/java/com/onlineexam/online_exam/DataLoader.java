
package com.onlineexam.online_exam;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner loadData(
            ExamRepository examRepository,
            QuestionRepository questionRepository) {

        return args -> {

            if (examRepository.count() == 0) {
                examRepository.save(
                    new Exam("Java Programming", "Java", 30, 20));

                examRepository.save(
                    new Exam("Database Management Systems", "DBMS", 30, 20));

                examRepository.save(
                    new Exam("Computer Networks", "CN", 45, 30));
            }

            if (questionRepository.count() == 0) {

                Exam javaExam = examRepository.findAll()
                    .stream()
                    .filter(e -> "Java".equalsIgnoreCase(e.getSubject()))
                    .findFirst()
                    .orElseThrow();

                questionRepository.save(new Question(
                    javaExam.getId(),
                    "Which keyword is used to inherit a class in Java?",
                    "implements", "extends", "inherits", "super",
                    "B", 4));

                questionRepository.save(new Question(
                    javaExam.getId(),
                    "Which method is the entry point of a Java application?",
                    "start()", "run()", "main()", "init()",
                    "C", 4));

                questionRepository.save(new Question(
                    javaExam.getId(),
                    "Which of these is not a primitive data type in Java?",
                    "int", "char", "String", "boolean",
                    "C", 4));

                questionRepository.save(new Question(
                    javaExam.getId(),
                    "Which keyword is used to create an object in Java?",
                    "class", "new", "this", "void",
                    "B", 4));

                questionRepository.save(new Question(
                    javaExam.getId(),
                    "Which collection does not allow duplicate elements?",
                    "List", "ArrayList", "Set", "Queue",
                    "C", 4));
            }
        };
    }
}

