# Application & Examination Security Specifications
**College Online Examination System**

---

## 1. Zero-Trust Examination Design

Standard web applications often make the mistake of transmitting answers or scores to the client browser and trusting the submitted result. In an academic examination environment, students have complete access to the browser console, network inspect tabs, and DOM tree.

Our application implements **Strict Zero-Trust Principles**:
1. **Answer Keys Never Sent to Browser:** The `correctAnswer` attribute of questions is stripped on the backend and never passed into the Thymeleaf model for `exam.html`.
2. **Server-Side Grading:** The student's browser submits only an array of chosen keys (`question_1=B`, `question_2=C`). Grading takes place exclusively inside Spring Boot using authoritative repository records.
3. **Atomic Quota Validation:** A student cannot bypass the 3-attempt limit by opening multiple tabs or sending rapid concurrent requests. Attempt increments are committed to the database atomically before questions are returned.

---

## 2. Strict 3-Attempt Policy Rules

* **Default Ceiling:** Exactly three attempts are permitted per exam per student.
* **Attempt Counting:** An attempt is recorded the moment a test session starts.
* **Exhaustion State:** Once 3 attempts have been completed or aborted, the system marks the exam as `Attempts Exhausted`. Further navigation to `/exam/{id}` immediately redirects to `/exams?error=attempts_exhausted`.
* **Audited Reset Capability:** If a student suffers an unavoidable system failure, an authorized College Administrator can reset the quota through the Admin Audit Portal (`/admin/audit-logs`), creating a permanent audit trail.

---

## 3. Browser Integrity Monitoring Rules

1. **Tab Switch & Minimize Detection:** Listens to `document.visibilitychange`. If the examination window is hidden, a violation event is recorded.
2. **Focus Loss Detection:** Listens to `window.blur`. If another application gains focus, a violation is recorded.
3. **Copy-Paste & Context Menu Suppression:** Right-click context menus and keyboard copy shortcuts (`Ctrl+C`, `Ctrl+V`, `Cmd+C`) are disabled to discourage prompt leakage.
4. **Three-Strike Rule:**
   * Violation 1: Warning modal popup.
   * Violation 2: Final warning modal popup.
   * Violation 3: Immediate server auto-submission. The attempt is finalized under status `AUTO_SUBMITTED_VIOLATION`.

---

## 4. Role-Based Access Control (RBAC)

Spring Security strictly enforces route-level authorization:
* `/admin/**`: Restricted to `ROLE_ADMIN`.
* `/faculty/**`: Restricted to `ROLE_FACULTY`.
* `/student/**`, `/exams/**`, `/exam/**`, `/results/**`: Restricted to `ROLE_STUDENT` (and Admin).
* Anonymous access is limited to `/`, `/login`, static CSS/JS assets, and the Firebase configuration endpoint.

