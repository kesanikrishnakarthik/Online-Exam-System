# Architecture Design Document
**College Online Examination System**  
**Institution:** Apex Institute of Technology  
**Project ID:** `online-exams-98fa6`

---

## 1. Architectural Philosophy

The application balances trusted server-side academic governance with modern real-time cloud capabilities:

1. **Trusted Authority Pattern (Zero-Trust Browser):**
   * Students' browsers can be manipulated via Developer Tools. Therefore, **correct answer keys are never sent to the client** before an exam is submitted.
   * Attempt counts, remaining quotas, and final grades are strictly calculated, validated, and finalized by the Spring Boot server.
2. **Cloud Firestore Real-Time Synchronization:**
   * Uses Cloud Firestore for real-time document synchronization (`examAttempts`, `results`, `examViolations`).
   * Evaluators and faculty can inspect live attempt activity from anywhere.
3. **Decoupled Three-Tier Architecture:**
   * **Presentation Tier:** Responsive Thymeleaf views, HTML5, CSS3, Firebase Web SDK, and JavaScript anti-cheating listeners.
   * **Application / Logic Tier:** Spring Boot on Java 25, Spring Security with BCrypt hashing, atomic attempt validators, and transaction handlers.
   * **Persistence Tier:** Cloud Firestore (`online-exams-98fa6`) for cloud documents + H2 Database for local relational integrity and automated tests.

---

## 2. Security Boundaries & Authorization Flow

```
[Browser Client]
       │
       ├── 1. POST /login (Credentials: user/faculty/admin)
       ▼
[Spring Security Filter Chain]
       │
       ├── Authenticates credentials via BCryptPasswordEncoder
       ├── Assigns Authorities: ROLE_STUDENT | ROLE_FACULTY | ROLE_ADMIN
       ▼
[Custom AuthenticationSuccessHandler]
       ├── ROLE_ADMIN   ───> Redirect: /admin/dashboard
       ├── ROLE_FACULTY ───> Redirect: /faculty/dashboard
       └── ROLE_STUDENT ───> Redirect: /dashboard
```

---

## 3. Strict 3-Attempt Enforcement Sequence

```
Student Client                   Spring Boot Backend               Database / Firestore
      │                                   │                                 │
      ├─── 1. GET /exam/{id} ────────────>│                                 │
      │                                   ├── Query count of attempts ─────>│
      │                                   │<── Return attempt count (e.g. 3)┤
      │                                   │                                 │
      │                              [Attempts >= 3?]                       │
      │                                   │                                 │
      │<── Redirect: /exams?error ────────┤ (If YES: Blocked)               │
      │    "All 3 attempts used"          │                                 │
      │                                   │ (If NO: Allowed)                │
      │                                   ├── Insert new Attempt(IN_PROGRESS)─>│
      │<── Render exam.html ──────────────┤                                 │
      │    (Timer, Palette, Questions)    │                                 │
```

---

## 4. Browser Integrity & Anti-Cheating Architecture

* **Event Listeners:**
  * `document.visibilitychange`: Detects when student switches browser tabs or minimizes the window.
  * `window.blur`: Detects loss of window focus.
  * `document.contextmenu`: Suppresses right-click inspection.
  * `document.keydown`: Prevents clipboard copy (`Ctrl+C`, `Ctrl+V`, `Cmd+C`).
* **Warning Escalation:**
  * **Violation 1:** Displays security warning modal; logs incident to `/api/exam/{id}/violation` and Cloud Firestore.
  * **Violation 2:** Displays final warning modal; logs incident.
  * **Violation 3:** Backend forces immediate examination auto-submission (`submissionReason = "VIOLATION_LIMIT"`). Result is computed based on answered questions and locked.
