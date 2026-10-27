package com.onlineexam.online_exam;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long examId;

    @Column(length = 2000)
    private String questionText;

    // Supported Types: SINGLE_CHOICE, MULTIPLE_SELECT, FILL_BLANKS
    private String questionType = "SINGLE_CHOICE";

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    /**
     * Stores the answer key:
     * - SINGLE_CHOICE: Single option key (e.g. "B")
     * - MULTIPLE_SELECT: Comma-separated keys (e.g. "A,C" or "A,B,D")
     * - FILL_BLANKS: Comma-separated list of accepted valid answers (e.g. "object,object-oriented,oop")
     */
    @Column(length = 1000)
    private String correctAnswer;

    // Marking scheme for multiple-select: ALL_OR_NOTHING or PARTIAL_CREDIT
    private String markingScheme = "ALL_OR_NOTHING";

    // For fill-in-the-blanks case-sensitive matching
    private boolean caseSensitive = false;

    private int marks = 4;
    private int negativeMarks = 0;
    private int displayOrder = 1;

    public Question() {
    }

    // Constructor for backwards compatibility (Single Choice MCQ)
    public Question(Long examId, String questionText,
                    String optionA, String optionB,
                    String optionC, String optionD,
                    String correctAnswer, int marks) {
        this.examId = examId;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.marks = marks;
        this.questionType = "SINGLE_CHOICE";
    }

    // Comprehensive Constructor for all Question Types
    public Question(Long examId, String questionText, String questionType,
                    String optionA, String optionB, String optionC, String optionD,
                    String correctAnswer, String markingScheme, boolean caseSensitive,
                    int marks, int negativeMarks) {
        this.examId = examId;
        this.questionText = questionText;
        this.questionType = (questionType != null && !questionType.isBlank()) ? questionType : "SINGLE_CHOICE";
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.markingScheme = (markingScheme != null && !markingScheme.isBlank()) ? markingScheme : "ALL_OR_NOTHING";
        this.caseSensitive = caseSensitive;
        this.marks = marks;
        this.negativeMarks = negativeMarks;
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

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionType() {
        return questionType != null ? questionType : "SINGLE_CHOICE";
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getMarkingScheme() {
        return markingScheme != null ? markingScheme : "ALL_OR_NOTHING";
    }

    public void setMarkingScheme(String markingScheme) {
        this.markingScheme = markingScheme;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public int getMarks() {
        return marks;
    }

    public void setMarks(int marks) {
        this.marks = marks;
    }

    public int getNegativeMarks() {
        return negativeMarks;
    }

    public void setNegativeMarks(int negativeMarks) {
        this.negativeMarks = negativeMarks;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
