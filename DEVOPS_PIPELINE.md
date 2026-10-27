# DevOps CI/CD Pipeline Documentation
**College Online Examination System**

---

## 1. Pipeline Overview

The project demonstrates a production-grade DevOps Continuous Integration and Continuous Deployment (CI/CD) lifecycle:

```
[ Git Commit ] ➔ [ Jenkins Trigger ] ➔ [ Environment Verification ] 
       ➔ [ Automated Unit Testing ] ➔ [ Test Report Publishing ]
       ➔ [ Maven Package Build ] ➔ [ Multi-Stage Docker Build ] 
       ➔ [ Container Deployment ] ➔ [ Health Verification ]
```

---

## 2. CI/CD Stage Breakdown (`Jenkinsfile`)

### Stage 1: Checkout from GitHub
* Pulls the latest commits from `https://github.com/kesanikrishnakarthik/Online-Exam-System.git` on branch `main`.

### Stage 2: Environment Verification
* Validates that Java 25, Maven, and Docker daemons are installed and operational:
  ```bash
  java -version
  mvn -version
  docker --version
  ```

### Stage 3: Run Automated Tests
* Runs all domain, security, attempt quota, and scoring unit tests:
  ```bash
  mvn test
  ```
* Publishes JUnit XML test reports to Jenkins for historical test pass/fail graphing.

### Stage 4: Build Application
* Assembles the Spring Boot executable fat JAR inside the build workspace:
  ```bash
  mvn clean package -DskipTests
  ```

### Stage 5: Build Docker Image
* Creates the optimized container image `online-exam:1.0` using multi-stage compilation on Eclipse Temurin 25:
  ```bash
  docker build -t online-exam:1.0 .
  ```

### Stage 6: Deploy Application
* Stops and removes any previously running container instance to avoid port conflicts, and launches the new release:
  ```bash
  docker rm -f online-exam-container 2>NUL
  docker run -d --name online-exam-container -p 8081:8081 online-exam:1.0
  ```

### Stage 7: Health Check & Verification
* Confirms container status and availability at `http://localhost:8081`.

---

## 3. Container Optimization (`Dockerfile`)

* **Multi-Stage Build:**
  * **Build Stage:** `eclipse-temurin:25-jdk` copies source files and compiles the JAR.
  * **Runtime Stage:** `eclipse-temurin:25-jre` runs only the minimal Java runtime environment, reducing the final image size and attack surface.
* **Security & Cleanliness:**
  * `.dockerignore` prevents `.git`, sensitive environment files (`.env`), and build targets from leaking into the container image.
