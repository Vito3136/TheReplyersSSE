# Vulnerable Web Application (Java 17 + Spring Boot)

This intentionally insecure web application is designed for educational
purposes—specifically to practice **Secure Software Engineering** techniques
such as static and dynamic analysis, vulnerability detection, and remediation.

## Features (and intentional flaws)

* **Signup / Login / Logout** — credentials are stored in **plain text**.
* **SQL Injection** — the login process concatenates user inputs directly in
  SQL statements.
* **Reflected & Stored XSS**
  * Reflected: input on the main page is echoed back unsanitized.
  * Stored: user‑supplied messages / file content are saved and later rendered
    with `th:utext`, bypassing HTML escaping.
* **Insecure File Upload** — no checks on file type or content.
* **Missing CSRF Protection**, **weak session management**, and much more.

> **Do _not_ deploy this application on the public Internet.** It is unsafe by
> design.

## Quick start

```bash
mvn spring-boot:run
```

The application will create an `h2` database file (`vulnappdb.mv.db`) in the
project root on first launch.

## Database schema

You can also initialize the database manually:

```bash
java -cp h2*.jar org.h2.tools.RunScript -url jdbc:h2:./vulnappdb -script create_db.sql
```
