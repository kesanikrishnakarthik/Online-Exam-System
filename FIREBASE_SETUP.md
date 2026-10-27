# Firebase Setup Guide for Online Exam System
**Project ID:** `online-exams-98fa6`

This document details the exact steps to verify and configure Firebase for the College Online Examination System.

---

## 1. Firebase Project Overview

* **Project ID:** `online-exams-98fa6`
* **Auth Domain:** `online-exams-98fa6.firebaseapp.com`
* **Storage Bucket:** `online-exams-98fa6.firebasestorage.app`
* **App ID:** `1:204869742765:web:22b7939b4a550f40a55624`
* **Measurement ID:** `G-NTKWQRBQT2`

---

## 2. Step-by-Step Console Activation

### A. Enable Firebase Authentication
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Select your project: **online-exams-98fa6**.
3. In the left navigation menu, click **Build** > **Authentication**.
4. Click **Get Started**.
5. Under the **Sign-in method** tab, click **Email/Password**.
6. Switch **Enable** to **ON** and click **Save**.
7. Under the **Users** tab, add test accounts matching the college roles:
   * **Student:** `user@college.edu` with password `Exam@123`
   * **Faculty:** `faculty@college.edu` with password `Faculty@123`
   * **Admin:** `admin@college.edu` with password `Admin@123`

### B. Enable Cloud Firestore
1. In the left navigation menu, click **Build** > **Firestore Database**.
2. Click **Create database**.
3. Choose a Firestore location close to your region (e.g., `asia-south1` or `us-central1`).
4. Select **Start in production mode**.
5. Click **Create**.

### C. Deploy Firestore Security Rules
1. In the Firestore Database section, open the **Rules** tab.
2. Replace any default rules with the contents of the `firestore.rules` file in this repository.
3. Click **Publish**.

---

## 3. Collections Structure in Cloud Firestore

| Collection | Description | Document Fields |
| :--- | :--- | :--- |
| `colleges` | College institutions | `collegeId`, `name`, `code`, `establishedYear` |
| `users` | Student, Faculty, Admin profiles | `uid`, `email`, `role`, `college`, `department`, `status` |
| `exams` | Examination configurations | `id`, `title`, `subject`, `durationMinutes`, `totalMarks`, `maxAttempts`, `maxViolations` |
| `questions` | MCQs with options | `id`, `examId`, `questionText`, `optionA`, `optionB`, `optionC`, `optionD`, `marks` |
| `examAttempts` | Student exam attempts | `attemptId`, `examId`, `studentUsername`, `attemptNumber`, `violationCount`, `status`, `startTime` |
| `results` | Official graded results | `resultId`, `examId`, `studentUsername`, `score`, `totalMarks`, `percentage`, `submissionTime` |
| `examViolations`| Anti-cheating violation logs | `attemptId`, `studentUsername`, `violationType`, `violationNumber`, `timestamp` |

---

## 4. Client-Side Script Verification

The frontend automatically includes `/js/firebase-config.js` on all exam templates.
The Firebase App, Firestore, and Analytics compatibility SDKs are loaded before
this configuration script. Analytics initializes when supported by the browser.
When browsing any page, you will notice the live badge in the navigation bar:
`🟢 Firebase: online-exams-98fa6`

This confirms your project's connection status!
