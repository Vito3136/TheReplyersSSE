# VulnApp - Vulnerable Web Application (Java 17 + Spring Boot) 🛡️💻

<img width="1203" height="712" alt="image (1)" src="https://github.com/user-attachments/assets/4eddc40d-ed61-40fe-88fc-0fdb9e4fce56" />

**VulnApp** is an intentionally insecure web application designed exclusively for educational purposes. It serves as a practical environment to study, test, and implement **Secure Software Engineering** techniques, including static application security testing (SAST), dynamic application security testing (DAST), and cryptographic vulnerability remediation.

---

## 📂 Repository Structure

The repository is organized into two primary branches to clearly demonstrate the lifecycle of vulnerability exploitation and remediation:
* **`MASTER` Branch:** Contains the original, vulnerable version of the Web Application, featuring unparameterized queries, legacy cryptographic hashing, and missing session headers.
* **`FIXES` Branch:** Contains the secure version of the Web Application, where all identified flaws have been patched using modern engineering standards.

---

## 🚀 Getting Started

On the very first launch, the application automatically provisions an embedded **h2 database file** (`vulnappdb.mv.db`) directly within the project root folder. No manual SQL initialization is required.

### Core Features
* **Authentication:** Secure Login and Sign-up functionality.
* **Quick Message Dashboard:** An interactive public wall for user messages.
* **File Upload Area:** Profile or document storage workflows.
* **Network Testing Utilities:** Functional Send Ping and Ping Received features.
* **User Controls:** Personal account settings and secure logout mechanisms.

<img width="821" height="517" alt="image (2)" src="https://github.com/user-attachments/assets/654189fd-9555-40c9-a905-42e38f680949" />

---

## 📊 Security Assessment & Vulnerability Analysis

The development process involved rigorous automated testing to identify architectural and programmatic security flaws.

### 1. Static Code Analysis (Fortify SCA)
A full repository scan using **Fortify SCA** highlighted a total of **52 vulnerabilities** distributed across 18 distinct categories:
* 🔴 **3 Critical Flaws**
* 🟠 **23 High-Severity Flaws**
* 🟡 **1 Medium-Severity Flaws**
* 🟢 **25 Low-Severity Flaws**

#### Key Findings & Implementations:
* **SQL Injection (SQLi):** The vulnerable branch was susceptible to Authentication Bypass (using payload strings like `" OR '1'='1'--"`) as well as data destruction via Stacked-Based SQLi (`DELETE FROM` injections) and Error-Based query leakage. 
  * *The Fix:* Implemented programmatic **Prepared Statements** to strictly isolate user input from SQL commands, combined with a strict alphanumeric regex validation filter (`^[a-zA-Z0-9_.-]{3,30}$`) on inputs.
* **Weak Cryptographic Hash:** User passwords were saved using the obsolete MD5 hashing algorithm, making them highly vulnerable to brute-force and collision attacks.
  * *The Fix:* Created a robust `PswdHash` layer utilizing **PBKDF2 with HMAC-SHA256**. The architecture forces **65,536 iterations**, a 256-bit key length, and integrates cryptographically secure random 16-byte salting to completely neutralize precomputed rainbow table attacks.

---

### 2. Dynamic Code Analysis (OWASP ZAP)
Automated application scanning via **OWASP ZAP** identified **8 dynamic vulnerabilities**, which were augmented by manual penetration testing:
* 🔴 **2 High-Risk Alerts** (including Path Traversal and SQLi)
* 🟠 **3 Medium-Risk Alerts** (Missing Anti-CSRF and Missing CSP Headers)
* 🟡 **1 Low-Risk Alert** (Missing Anti-Clickjacking configuration)
* 🔵 **2 Informational Alerts**

#### Key Findings & Implementations:
* **Cross-Site Scripting (Stored XSS):** The Quick Message wall allowed unescaped HTML injection. An attacker could store arbitrary JavaScript (e.g., `<script>alert('Hacked')</script>`), executing it in the browser session of every visiting user.
  * *The Fix:* Replaced all unescaped Thymeleaf frontend tags (`th:utext`) with safe, auto-escaping tags (`th:text`). Additionally, added a server-side sanitization layer utilizing `StringEscapeUtils.escapeHtml4()` prior to data rendering.
* **Absence of Anti-CSRF Tokens:** State-changing POST requests lacked verification, exposing authenticated users to Cross-Site Request Forgery risks.
  * * *The Fix:* Configured a synchronized token pattern. A cryptographically random UUID token is generated on every `GET` workflow, attached to both the user session and the view model, and systematically validated upon receiving subsequent `POST` actions.

---

Developed as an academic research project at the **University of Bari Aldo Moro**.
