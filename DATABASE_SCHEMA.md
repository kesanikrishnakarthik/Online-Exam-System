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
* `institutionalId` (VARCHAR): Student College Roll / Hall Ticket Number or Faculty Employee ID
* `designation` (VARCHAR): Faculty title/designation (e.g., "Assistant Professor", "HOD")
* `role` (VARCHAR): Security Role (`ROLE_PLATFORM_ADMIN`, `ROLE_ADMIN`, `ROLE_FACULTY`, `ROLE_STUDENT`)
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
* `subjectCode` (VARCHAR): Course/Subject identifier (e.g., "CS301")
* `instructions` (TEXT): Exam-taking directives and proctoring policy
* `durationMinutes` (INT): Total time allocated (e.g., 30)
* `totalMarks` (INT): Calculated sum of question marks
* `passMarks` (INT): Minimum score threshold for passing (defaults to 40% of totalMarks)
* `negativeMarking` (BOOLEAN): Enables penalty deduction for incorrect choices
* `college` (VARCHAR): College institution name (e.g., "Apex Institute of Technology")
* `collegeCode` (VARCHAR, Indexed): Tenant identifier ("AIT", "NIE")
* `department` (VARCHAR): Academic department
* `branchProgram` (VARCHAR): Target program (e.g., "B.Tech CSE")
* `academicYear` (VARCHAR): Target year
* `semester` (VARCHAR): Target semester
* `maxAttempts` (INT): Maximum allowed attempts per student (Default: `3`)
* `maxViolations` (INT): Maximum allowed integrity violations (Default: `3`)
* `published` (BOOLEAN): Publication status
* `lifecycleStatus` (VARCHAR): `DRAFT`, `PUBLISHED`, `CLOSED`

### D. `Question` (Item Bank Entity)
* `id` (BIGINT, Primary Key, Auto-increment)
* `examId` (BIGINT, Foreign Key, Indexed): Linked Exam ID
* `questionText` (TEXT): The question prompt
* `questionType` (VARCHAR): Item type: `SINGLE_CHOICE` (MCQ), `MULTIPLE_SELECT` (Checkboxes), or `FILL_BLANKS`
* `optionA` (TEXT): Choice A (Used for SINGLE_CHOICE and MULTIPLE_SELECT)
* `optionB` (TEXT): Choice B
* `optionC` (TEXT): Choice C
* `optionD` (TEXT): Choice D
* `correctAnswer` (VARCHAR, Length 1000): Answer key:
  - Single Choice: Single option key (e.g., `B`)
  - Multiple Select: Comma-separated option keys (e.g., `A,C`)
  - Fill-in-the-Blanks: Comma-separated accepted variations (e.g., `metaspace, method area, permgen`)
* `markingScheme` (VARCHAR): For multiple-select: `ALL_OR_NOTHING` (exact set match) or `PARTIAL_CREDIT` (proportional)
* `caseSensitive` (BOOLEAN): For fill-in-the-blanks case-sensitive text matching
* `marks` (INT): Marks assigned (e.g., 4)
* `negativeMarks` (INT): Penalty deductions for wrong responses
* `displayOrder` (INT): Sequence ordering within the exam

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
* `rollNumber` (VARCHAR): Candidate institutional roll number
* `college` (VARCHAR)
* `collegeCode` (VARCHAR, Indexed)
* `score` (INT)
* `totalMarks` (INT)
* `percentage` (DOUBLE): Precalculated score percentage
* `passFailStatus` (VARCHAR): Academic evaluation (`PASS` or `FAIL`)
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

---

## 4. Database Persistence & Docker Volume Storage

To prevent data loss on restarts, CollegeConnect uses a persistent file-based H2 database engine:
- **JDBC Connection URL:** `jdbc:h2:file:./data/onlineexamdb;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1`
- **Driver:** `org.h2.Driver`
- **Hibernate DDL Mode:** `update` (preserves existing schema and registered rows, auto-applying incremental columns)
- **Container Persistence:** `docker-compose.yml` mounts the host `./data` directory into the container's `/app/data` volume so all registered colleges, students, faculty, exams, and results survive container updates and system reboots.
