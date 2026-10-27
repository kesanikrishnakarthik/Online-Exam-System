# Deployment Guide
**College Online Examination System**

---

## 1. Local Run via Maven

```powershell
cd online-exam
.\mvnw.cmd spring-boot:run
```
The application will start on port `8081`. Access at `http://localhost:8081`.

---

## 2. Docker Container Deployment

### Build the Image
```bash
docker build -t online-exam:1.0 .
```

### Run the Container
```bash
docker run -d --name online-exam-container -p 8081:8081 online-exam:1.0
```

### Stop and Clean Up
```bash
docker stop online-exam-container
docker rm online-exam-container
```

---

## 3. Cloud Server Deployment (AWS EC2 / DigitalOcean / Azure VM)

1. Provision an Ubuntu 22.04 LTS or Debian VM.
2. Install Docker and Git:
   ```bash
   sudo apt-get update
   sudo apt-get install -y docker.io git
   sudo systemctl enable --now docker
   ```
3. Clone repository and build:
   ```bash
   git clone https://github.com/kesanikrishnakarthik/Online-Exam-System.git
   cd Online-Exam-System
   docker build -t online-exam:1.0 .
   docker run -d --restart always --name online-exam-app -p 80:8081 online-exam:1.0
   ```
4. Configure VM Firewall (Security Groups) to allow inbound TCP on port `80` (HTTP) and `443` (HTTPS).

