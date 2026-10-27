# Database Schema & Data Models
**College Online Examination System**

---

## 1. Relational Entities (Spring Data JPA)

### A. `Exam`
* `id` (BIGINT, Primary Key)
* `title` (VARCHAR): Examination title
* `subject` (VARCHAR): Subject name
* `durationMinutes` (INT): Total time allocated (e.g., 30)
* `totalMarks` (INT): Calculated sum of question marks
* `college` (VARCHAR): College institution (e.g., "Apex Institute of Technology")
* `department` (VARCHAR): Department (e.g., "Computer Science & Engineering")
* `maxAttempts` (INT): Maximum allowed attempts per student (Default: `3`)
* `maxViolations` (INT): Maximum allowed integrity violations (Default: `3`)
* `published` (BOOLEAN): Publication status

### B. `Question`
* `id` (BIGINT, Primary Key)
* `examId` (BIGINT, Foreign Key): Linked Exam ID
* `questionText` (TEXT): The question prompt
* `optionA` (TEXT): Choice A
* `optionB` (TEXT): Choice B
* `optionC` (TEXT): Choice C
* `optionD` (TEXT): Choice D
* `correctAnswer` (VARCHAR): Correct option key (`A`, `B`, `C`, or `D`)
* `marks` (INT): Marks assigned (e.g., 4)

### C. `ExamAttempt`
* `id` (BIGINT, Primary Key)
* `examId` (BIGINT): Linked Exam ID
* `examTitle` (VARCHAR): Snapshot of exam title
* `subject` (VARCHAR): Subject
* `studentUsername` (VARCHAR): Username of the student
* `college` (VARCHAR): Student's college
* `attemptNumber` (INT): Sequence number (`1`, `2`, or `3`)
* `startTime` (TIMESTAMP): Time attempt began
* `submissionTime` (TIMESTAMP): Time attempt concluded
* `score` (INT): Final score
* `totalMarks` (INT): Maximum marks
* `violationCount` (INT): Logged security violations
* `status` (VARCHAR): `IN_PROGRESS`, `COMPLETED`, `AUTO_SUBMITTED_VIOLATION`, `AUTO_SUBMITTED_TIMEOUT`
* `submissionReason` (VARCHAR): `NORMAL`, `VIOLATION_LIMIT`, `TIMEOUT`

### D. `ExamResult`
* `id` (BIGINT, Primary Key)
* `examId` (BIGINT)
* `examTitle` (VARCHAR)
* `subject` (VARCHAR)
* `studentUsername` (VARCHAR)
* `college` (VARCHAR)
* `score` (INT)
* `totalMarks` (INT)
* `attemptNumber` (INT)
* `submissionTime` (TIMESTAMP)
* `violationCount` (INT)
* `submissionReason` (VARCHAR)

---

## 2. Cloud Firestore Collections Structure (`online-exams-98fa6`)

```
/colleges/{collegeId}
    - name: "Apex Institute of Technology"
    - code: "AIT-ENG-2026"
    - departments: ["CSE", "IT", "ECE", "AIML"]

/users/{userId}
    - email: "user@college.edu"
    - role: "STUDENT" | "FACULTY" | "ADMIN"
    - college: "Apex Institute of Technology"

/exams/{examId}
    - title: "Java Programming"
    - subject: "Java"
    - durationMinutes: 30
    - maxAttempts: 3
    - maxViolations: 3

/examAttempts/{attemptId}
    - examId: 1
    - studentUsername: "user"
    - attemptNumber: 1
    - status: "COMPLETED"
    - violationCount: 0
    - syncedAt: Timestamp

/results/{resultId}
    - examId: 1
    - studentUsername: "user"
    - score: 16
    - totalMarks: 20
    - percentage: 80.0
    - attemptNumber: 1
    - submissionReason: "NORMAL"
    - syncedAt: Timestamp

/examViolations/{violationId}
    - examId: 1
    - studentUsername: "user"
    - violationReason: "Switched browser tab or minimized window"
    - violationNumber: 1
    - timestamp: Timestamp
```

