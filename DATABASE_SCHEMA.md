# Database Schema & Multi-Tenant Data Models
**CollegeConnect - Multi-College Online Examination Platform**

---

## 1. Relational Entities (Spring Data JPA)

### A. `College` (Tenant Entity)
Stores registered higher education institutions, accreditation codes, contact leadership, and tenant lifecycle status.
* `id` (BIGINT, Primary Key, Auto-increment)
* `name` (VARCHAR): Institutional Full Name (e.g., "Apex Institute of Technology")
* `code` (VARCHAR, Unique, Indexed): Institutional Short Code (e.g., "AIT", "NIE", "SXC")
* `type` (VARCHAR): Institution Type (e.g., "Engineering & Technology", "Autonomous University")
* `status` (VARCHAR): Lifecycle status (`APPROVED`, `PENDING`, `SUSPENDED`, `REJECTED`)
* `address` (VARCHAR): Campus Address & City
* `contactPerson` (VARCHAR): Dean / Principal / HOD Name
* `contactEmail` (VARCHAR): Official Institutional Email
* `contactPhone` (VARCHAR): Contact Telephone Number
* `departments` (VARCHAR): Comma-separated list of approved academic departments
* `createdAt` (TIMESTAMP): Date registered

### B. `UserAccount` (Institutional User Entity)
Centralized user authentication and institutional identity mapping.
* `id` (BIGINT, Primary Key, Auto-increment)
* `username` (VARCHAR, Unique, Indexed): Normalized login username (e.g., `user`, `ait_21b91a0501`, `superadmin`)
* `password` (VARCHAR): BCrypt-hashed password
* `fullName` (VARCHAR): User's legal name
* `email` (VARCHAR): Institutional email
* `rollNumberOrFacultyId` (VARCHAR): Student College Roll / Hall Ticket Number or Faculty Employee ID
* `role` (VARCHAR): Security Role (`ROLE_PLATFORM_ADMIN`, `ROLE_COLLEGE_ADMIN`, `ROLE_FACULTY`, `ROLE_STUDENT`)
* `collegeCode` (VARCHAR, Indexed): Associated College Tenant Code (e.g., "AIT", "NIE")
* `collegeName` (VARCHAR): Institution Name snapshot
* `department` (VARCHAR): Academic department (e.g., "Computer Science & Engineering")
* `degreeBranch` (VARCHAR): e.g., "B.Tech CSE", "B.Tech ECE"
* `academicYear` (VARCHAR): e.g., "3rd Year"
* `semester` (VARCHAR): e.g., "1st Semester"
* `section` (VARCHAR): e.g., "A", "B"
* `status` (VARCHAR): Account status (`ACTIVE`, `PENDING`, `SUSPENDED`)
* `createdAt` (TIMESTAMP): Account registration timestamp

### C. `Exam` (Assessment Entity)
Partitioned by tenant `collegeCode`.
* `id` (BIGINT, Primary Key, Auto-increment)
* `title` (VARCHAR): Examination title
* `subject` (VARCHAR): Subject name
* `durationMinutes` (INT): Total time allocated (e.g., 30)
* `totalMarks` (INT): Calculated sum of question marks
* `college` (VARCHAR): College institution name (e.g., "Apex Institute of Technology")
* `collegeCode` (VARCHAR, Indexed): Tenant identifier ("AIT", "NIE")
* `department` (VARCHAR): Academic department
* `branchProgram` (VARCHAR): Target program (e.g., "B.Tech CSE")
* `academicYear` (VARCHAR): Target year
* `semester` (VARCHAR): Target semester
* `maxAttempts` (INT): Maximum allowed attempts per student (Default: `3`)
* `maxViolations` (INT): Maximum allowed integrity violations (Default: `3`)
* `published` (BOOLEAN): Publication status
* `lifecycleStatus` (VARCHAR): `DRAFT`, `SCHEDULED`, `LIVE`, `CONCLUDED`

### D. `Question` (Item Bank Entity)
* `id` (BIGINT, Primary Key, Auto-increment)
* `examId` (BIGINT, Foreign Key, Indexed): Linked Exam ID
* `questionText` (TEXT): The question prompt
* `optionA` (TEXT): Choice A
* `optionB` (TEXT): Choice B
* `optionC` (TEXT): Choice C
* `optionD` (TEXT): Choice D
* `correctAnswer` (VARCHAR): Correct option key (`A`, `B`, `C`, or `D`)
* `marks` (INT): Marks assigned (e.g., 4)

### E. `ExamAttempt` (Integrity & Session Entity)
Logs every ongoing and concluded exam session.
* `id` (BIGINT, Primary Key, Auto-increment)
* `examId` (BIGINT, Indexed): Linked Exam ID
* `examTitle` (VARCHAR): Snapshot of exam title
* `subject` (VARCHAR): Subject
* `studentUsername` (VARCHAR, Indexed): Username of candidate
* `college` (VARCHAR): College name
* `collegeCode` (VARCHAR, Indexed): Tenant code
* `attemptNumber` (INT): Sequence number (`1`, `2`, or `3`)
* `startTime` (TIMESTAMP): Time attempt began
* `submissionTime` (TIMESTAMP): Time attempt concluded
* `score` (INT): Final score achieved
* `totalMarks` (INT): Maximum marks
* `violationCount` (INT): Logged security violations (0 to 3)
* `status` (VARCHAR): `IN_PROGRESS`, `COMPLETED`, `AUTO_SUBMITTED_VIOLATION`, `AUTO_SUBMITTED_TIMEOUT`
* `submissionReason` (VARCHAR): `NORMAL`, `VIOLATION_LIMIT`, `TIMEOUT`

### F. `ExamResult` (Audited Score Entity)
Immutable audit record of verified final results.
* `id` (BIGINT, Primary Key, Auto-increment)
* `examId` (BIGINT, Indexed)
* `examTitle` (VARCHAR)
* `subject` (VARCHAR)
* `studentUsername` (VARCHAR, Indexed)
* `college` (VARCHAR)
* `collegeCode` (VARCHAR, Indexed)
* `score` (INT)
* `totalMarks` (INT)
* `attemptNumber` (INT)
* `submissionTime` (TIMESTAMP)
* `violationCount` (INT)
* `submissionReason` (VARCHAR)

---

## 2. Cloud Firestore Schema (`online-exams-98fa6`)

Cloud Firestore is organized into tenant-segregated collections with strict security rules:

```
colleges/{collegeCode}
  ├── name: string
  ├── code: string
  ├── status: "APPROVED" | "PENDING" | "SUSPENDED"
  └── exams/{examId}
        ├── title: string
        ├── durationMinutes: number
        ├── maxAttempts: 3
        ├── questions/{questionId}
        └── attempts/{attemptId}
              ├── studentUsername: string
              ├── collegeCode: string
              ├── attemptNumber: 1 | 2 | 3
              ├── violationCount: 0 | 1 | 2 | 3
              ├── status: string
              └── score: number
```

---

## 3. Multi-Tenant Isolation Enforcement

1. **JPA Query Filtering:** Every query in `ExamRepository`, `ExamAttemptRepository`, and `ExamResultRepository` includes `WHERE collegeCode = :collegeCode` to mathematically prevent data leakage across institutions.
2. **Roll Number Resolution:** Students can log in directly with `COLLEGE_CODE:ROLL_NUMBER` (e.g., `AIT:21B91A0501`), which `CustomUserDetailsService` automatically resolves to the appropriate tenant record.
3. **Firestore Security Rules:** Read and write permissions check `request.auth.token.collegeCode == resource.data.collegeCode`.
