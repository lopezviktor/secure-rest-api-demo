[![CI](https://github.com/lopezviktor/secure-rest-api-demo/actions/workflows/ci.yml/badge.svg)](https://github.com/lopezviktor/secure-rest-api-demo/actions/workflows/ci.yml)
# Secure REST API Demo (Spring Boot)

This repository demonstrates production-ready backend patterns suitable for client work, emphasizing security, correctness, comprehensive testing, and continuous integration.

Backend demo project focused on **clean architecture, security-ready design and real-world backend practices** using **Spring Boot 3**, **PostgreSQL**, **Docker** and **Flyway**.

This repository is designed as a **portfolio-grade backend project**, not a tutorial.

---

## 🚀 Tech Stack

- **Java 17 (Temurin LTS)**
- **Spring Boot 3**
- **Spring Data JPA (Hibernate)**
- **PostgreSQL 16 (Dockerised)**
- **Flyway** – database versioning & migrations
- **Spring Security (JWT, stateless)**
- **Spring Boot Actuator** – health checks & observability endpoints
- **OpenAPI 3.1 / Swagger UI (springdoc)**
- **Maven**
- **JUnit 5**
- **Docker & Docker Compose**

---

## 🧠 What this project demonstrates

- Clean separation between **domain, persistence, security and configuration layers**
- Security-first design with **JWT-based stateless authentication** and **role-based authorization**
- Database-first approach using **Flyway migrations** with proper **JPA entity mapping**
- Ownership enforcement ensuring users can only manage their own tasks unless `ADMIN`
- Environment-based configuration using Spring Profiles (`dev`, `test`)
- Reproducible local and test environments via **Docker**, **Docker Compose**, and **Testcontainers**
- Fully documented API with **OpenAPI 3.1** and **Swagger UI**
- Centralized API error handling with proper HTTP semantics, including partial updates via HTTP PATCH
- Secure observability using **Spring Boot Actuator** with role-based access control and correct HTTP semantics (401 vs 403)

---

## 🔐 Security Model

- Authentication via **stateless JWT (Bearer tokens)**
- Passwords stored securely using **BCrypt**
- Roles:
  - `ADMIN`: can create, read, update and delete tasks of any user
  - `USER`: can create, read, update and delete only their own tasks

Authorization rules are enforced at both the endpoint and service layers.

---

## 🔎 Observability & Actuator

- Spring Boot Actuator is enabled to expose operational and health information.
- Exposed endpoints:
  - `/actuator/health` → **Public** (no authentication)
  - `/actuator/info` → **ADMIN only**
- All other `/actuator/**` endpoints are **explicitly denied**.
- Actuator endpoints are protected by the same **JWT-based security filter chain** as the API.
- Correct HTTP semantics are enforced:
  - **401 Unauthorized** → request not authenticated
  - **403 Forbidden** → authenticated but insufficient role

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

Profiles ensure isolation to avoid test pollution and unintended side effects.

---

## 🧪 Testing

### Integration Tests (Testcontainers)

This project uses **Testcontainers** to run integration tests against a real **PostgreSQL** instance in Docker, ensuring realistic and isolated test execution.

#### Requirements
- Docker Desktop running
- Java 17+

#### macOS local note

On recent versions of Docker Desktop for macOS, Testcontainers may fail with an error similar to:

```
client version is too old. Minimum supported API version is 1.44
```

This is a known compatibility issue between Docker Desktop and `docker-java`.

To fix it locally, create the following file in your home directory:

```bash
~/.docker-java.properties
```

With this content:

```properties
api.version=1.52
```

This file is **local-only** and **must not be committed** to the repository.

#### Run tests

```bash
./mvnw clean test
```

---

## 📄 API Documentation (Swagger / OpenAPI)

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`

The API is fully documented using **OpenAPI 3.1** and can be tested directly from Swagger UI.
JWT Bearer authentication is supported via the **Authorize** button.

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

- ✔ JWT-based authentication (stateless, Bearer tokens)
- ✔ Role-based authorization (ADMIN / USER)
- ✔ Ownership enforcement across all task operations
- ✔ Full CRUD operations for Tasks
- ✔ PATCH & DELETE endpoints with ownership enforcement and proper HTTP semantics
- ✔ Partial updates via HTTP PATCH (no-op supported, validation enforced)
- ✔ Centralized API error handling
- ✔ Database-first design with Flyway migrations
- ✔ Dockerised PostgreSQL environment (dev & test)
- ✔ Environment profiles separation (dev / test)
- ✔ Secure development-only data seeding (BCrypt)
- ✔ OpenAPI 3.1 documentation with Swagger UI
- ✔ Integration tests using Testcontainers (PostgreSQL)
- ✔ CI pipeline with automated build & test execution
- ✔ Pagination & sorting with Spring Data Pageable
- ✔ Optional filtering by task completion status (completed=true/false)
- ✔ Default sorting configuration with client override support
- ✔ Secure Actuator endpoints (health public, info ADMIN-only, others denied)
- ✔ Login rate limiting with Bucket4j (per-IP, headers exposed)

🔜 Next planned steps:
- CI/CD pipeline hardening
- API versioning (/api/v1)

---

## 👤 Author

**Víctor López**  
Software Engineer (BSc)  
Backend-focused – Java & Spring Boot

> This project is part of a larger backend-focused portfolio, oriented towards clean architecture, security and scalable system design.
