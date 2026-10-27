# Testing Guide & Quality Assurance
**College Online Examination System**

---

## 1. Automated Testing

Run the automated test suite locally via Maven wrapper:

```powershell
cd online-exam
.\mvnw.cmd test
```

### Verified Automated Test Scenarios (`ExamSystemUnitTests.java`):
1. **`testExamCreationDefaults`**: Verifies examinations initialize with a strict default 3-attempt limit and 3-violation threshold.
2. **`testStrictThreeAttemptLimit`**: Simulates sequential student attempts (`1`, `2`, `3`) and verifies that a 4th attempt is strictly denied.
3. **`testServerSideScoreCalculation`**: Verifies exact calculation of student scores, total marks, and percentages without leaking answers.
4. **`testAntiCheatingViolationThreshold`**: Tests warning escalation on Violation 1 and 2, and confirms automatic test finalization upon Violation 3 (`AUTO_SUBMITTED_VIOLATION`).
5. **`testTimerExpirationAutoSubmission`**: Verifies automatic test submission when countdown expires (`AUTO_SUBMITTED_TIMEOUT`).
6. **`testStudentResultPrivacy`**: Confirms students can only retrieve their own scores and attempts.

---

## 2. End-to-End Manual Testing Checklist

| Step | Action | Expected Result | Verified |
| :--- | :--- | :--- | :--- |
| **1** | Open `http://localhost:8081` | Homepage loads with hero and feature grid. Firebase badge shows `🟢 Firebase: online-exams-98fa6`. | [x] |
| **2** | Login as Student (`user` / `Exam@123`) | Redirects to `/dashboard`. Metrics show available exams, completed exams, and attempts. | [x] |
| **3** | Click "Browse Examinations" (`/exams`) | Shows available exams with remaining attempt counts (`3 Attempt(s) Remaining`). | [x] |
| **4** | Start Exam (`/exam/1`) | Starts Attempt #1. Running countdown timer and question palette appear. | [x] |
| **5** | Select answers for questions | Corresponding palette buttons change to green (answered status). | [x] |
| **6** | Switch browser tab or press Alt+Tab | Anti-cheating modal pops up: *"Security Warning #1: Switched browser tab or minimized window"*. | [x] |
| **7** | Switch tab twice more (Total 3 violations) | Warning #2 displays on second strike. On 3rd violation, exam auto-submits. | [x] |
| **8** | View Result Summary | Result page displays score, percentage, and notice: *"⚠️ Automatically Submitted: Institutional browser security threshold (3 warnings) reached"*. | [x] |
| **9** | Take Exam 2 more times (Total 3 attempts) | Remaining attempts decrement to `0`. Button changes to *"Attempts Exhausted"*. Attempt 4 is blocked. | [x] |
| **10** | Login as Faculty (`faculty` / `Faculty@123`) | Redirects to `/faculty/dashboard`. | [x] |
| **11** | Click "Student Performance" (`/faculty/results`) | Shows all student scores, attempt numbers, and violation counts. Filter by subject works. | [x] |
| **12** | Click "Export Results (CSV)" | Downloads `college-exam-results.csv` spreadsheet. | [x] |
| **13** | Login as Admin (`admin` / `Admin@123`) | Redirects to `/admin/dashboard`. College metrics and departments are displayed. | [x] |
| **14** | Go to Audit Logs (`/admin/audit-logs`) | Complete attempt audit trail shown. Clicking "Reset Quota" clears attempts for that student. | [x] |

