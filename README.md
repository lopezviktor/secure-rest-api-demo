# Secure REST API Demo (Spring Boot)

Backend demo project focused on **clean architecture, security-ready design and real-world backend practices** using **Spring Boot 3**, **PostgreSQL**, **Docker** and **Flyway**.

This repository is designed as a **portfolio-grade backend project**, not a tutorial.

---

## 🚀 Tech Stack

- **Java 17 (Temurin LTS)**
- **Spring Boot 3**
- **Spring Data JPA (Hibernate)**
- **PostgreSQL 16 (Dockerised)**
- **Flyway** – database versioning & migrations
- **Maven**
- **JUnit 5**
- **Docker & Docker Compose**

---

## 🧠 What this project demonstrates

- Clean separation between **domain, persistence and configuration layers**
- **Database-first approach** using Flyway migrations (no schema auto-generation shortcuts)
- Proper **JPA entity mapping** aligned with the SQL schema
- **Environment-based configuration** using Spring Profiles
- Safe development-only data seeding
- Reproducible local environment via Docker
- Architecture ready for **JWT authentication and role-based authorization**

---

## 🗄️ Database Design

### Users
- Unique email
- Role-based access (`USER`, `ADMIN`)
- Creation timestamp

### Tasks
- Owned by users (foreign key relationship)
- Completion status
- Proper cascading rules

The database schema is managed exclusively via **Flyway migrations**.

---

## 🧪 Environment Profiles

- **dev**
  - Demo users and tasks are automatically seeded
- **test**
  - No data seeding
  - Clean application context for testing

Profiles are isolated to avoid test pollution and unintended side effects.

---

## 🐳 Run Locally

Requirements:
- Docker
- Java 17

```bash
docker compose up -d
./mvnw spring-boot:run
```

PostgreSQL runs fully isolated inside Docker.

---

## 📌 Project Status

✔ Database & migrations
✔ Entities & repositories
✔ Environment configuration
✔ Dev-only data seeding

🔜 Next planned steps:
- REST controllers & service layer
- DTOs and validation
- JWT authentication & authorization
- OpenAPI / Swagger documentation

---

## 👤 Author

**Víctor López**  
Software Engineer (BSc)  
Backend-focused – Java & Spring Boot

> This project is part of a larger backend-focused portfolio, oriented towards clean architecture, security and scalable system design.