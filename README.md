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
- **Spring Security (JWT, stateless)**
- **OpenAPI 3.1 / Swagger UI (springdoc)**
- **Maven**
- **JUnit 5**
- **Docker & Docker Compose**

---

## 🧠 What this project demonstrates

- Clean separation between **domain, persistence, security and configuration layers**
- **Database-first approach** using Flyway migrations (no schema auto-generation shortcuts)
- Proper **JPA entity mapping** aligned with the SQL schema
- **Environment-based configuration** using Spring Profiles (`dev`, `test`)
- Safe **development-only data seeding** with BCrypt-hashed passwords
- Reproducible local environment via **Docker & Docker Compose**
- **JWT-based authentication** (stateless, Bearer tokens)
- **Role-based authorization** (`ADMIN`, `USER`)
- **Ownership enforcement** (users can only create tasks for themselves)
- Centralized **API error handling** (no 500s for business errors)
- Fully documented API using **OpenAPI 3.1** with **Swagger UI**
## 🔐 Security Model

- Authentication via **JWT (Bearer tokens)**
- Stateless security configuration (no sessions)
- Roles:
  - `ADMIN`: can create tasks for any user
  - `USER`: can only create tasks for themselves (ownership enforced server-side)
- Passwords stored using **BCrypt**

Authorization rules are enforced both at the endpoint level and in the service layer.


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

✔ Database schema & migrations (Flyway)
✔ JPA entities & repositories
✔ Environment profiles (`dev`, `test`)
✔ Dev-only data seeding
✔ REST controllers & service layer
✔ DTOs & validation
✔ JWT authentication (stateless)
✔ Role-based authorization
✔ Ownership enforcement (USER vs ADMIN)
✔ Centralized API error handling
✔ OpenAPI / Swagger documentation

🔜 Next planned steps:
- Integration tests (MockMvc / Testcontainers)
- Extended task endpoints (update, delete, pagination)
- API hardening & refinements

---

## 👤 Author

**Víctor López**  
Software Engineer (BSc)  
Backend-focused – Java & Spring Boot

> This project is part of a larger backend-focused portfolio, oriented towards clean architecture, security and scalable system design.