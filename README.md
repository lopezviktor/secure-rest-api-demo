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
- **Prometheus (metrics scraping)**

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

## 📊 Metrics & Prometheus

- The `/actuator/prometheus` endpoint is exposed for metrics scraping.
- Access is protected by a **static metrics token** (not JWT-based).
- The token is shared between the application and Prometheus via a file, enabling non-expiring, automation-friendly access.

**Security model:**
- Requests to `/actuator/prometheus` must include:
  ```
  Authorization: Bearer <metrics-token>
  ```
- Missing or invalid token → **401 Unauthorized**

**Configuration:**
- The application reads the token from:
  - `METRICS_TOKEN_FILE` (preferred)
  - or `METRICS_TOKEN` (fallback)
- The actual token value is stored in `observability/prometheus/token.txt` (git-ignored).

### Local Development (Metrics Enabled)

```bash
./scripts/run-dev.sh
```

- This script sets `METRICS_TOKEN_FILE` automatically.
- Normal API usage (auth, tasks) is unaffected.

---

## 📈 Grafana Dashboard (Core API Metrics)

This project includes a Grafana dashboard for visualizing real-time API behaviour, using Prometheus metrics as the data source and Grafana for visualization.

- **API Request rate (req/s)**
- **API Latency p95 (ms)**
- **API Client errors (4xx)**
- **API Server errors (5xx)**

![Grafana – Core API Metrics](docs/grafana-core-metrics.png)

The dashboard reacts live to API traffic, authentication failures, and error conditions, making it suitable for demonstration and portfolio purposes.

---

## 🛎️ Alerting (Grafana Alert Rules)

This project includes production-grade **Grafana alerts** designed to monitor API availability, server-side errors, and performance degradation in real time. These alerts help ensure system reliability and provide meaningful insights for on-call engineers and portfolio demonstrations.

### API Availability Alert

This alert tracks the overall availability of the API by monitoring Prometheus scrape status.

- **Alert name:** API is DOWN
- **Metric:** `up{job="secure-rest-api-demo"}`
- **Condition:** Fires when `up == 0`, indicating the API is unreachable
- **Pending period:** Includes a grace period to avoid flapping during short restarts or deployments
- **Auto-recovery:** Automatically returns to *Normal* when the API becomes reachable again

#### Alert lifecycle states:

- **Normal:** API is running and reachable
- **Pending:** API has just gone down; grace period active
- **Firing:** API confirmed down after the pending window

This approach mirrors real-world production alerting best practices by minimizing false positives during transient outages.

#### Manual validation steps:

1. Stop the application → Prometheus target status changes to **DOWN**
2. Grafana alert transitions from **Normal → Pending → Firing**
3. Restart the application → alert returns to **Normal**

#### Alert screenshots:

**API running (Normal):**  
![Alert Normal](docs/alerts/api-alert-normal.png)

**API restarting (Pending):**  
![Alert Pending](docs/alerts/api-alert-pending.png)

**API down (Firing):**  
![Alert Firing](docs/alerts/api-alert-firing.png)

---

### API 5xx Error Rate Alert

This alert detects server-side failures by monitoring the rate of HTTP 5xx responses, enabling early detection of backend issues before they cause full outages.

- **Alert name:** API 5xx Error Rate High
- **Metric:**
  ```promql
  increase(http_server_requests_seconds_count{status=~"5.."}[5m])
  ```
- **Condition:** Fires when the 5xx error rate exceeds a defined threshold over a rolling 5-minute window
- **Scope:** Focuses exclusively on server errors (5xx), explicitly excluding client errors (4xx)
- **Auto-recovery:** Automatically resolves when the error rate returns below the threshold

#### Manual validation steps:

1. Induce server-side errors (HTTP 5xx)
2. Observe alert transition to **Firing**
3. Remove error condition and confirm automatic recovery to **Normal**

#### Alert screenshots:

**5xx error rate – Normal:**  
![5xx Alert Normal](docs/alerts/5xx-alert-normal.png)

**5xx error rate – Firing:**  
![5xx Alert Firing](docs/alerts/5xx-alert-firing.png)

---

### API Latency (p95) Alert

This alert monitors the 95th percentile latency (p95) of API requests, reflecting real user experience more accurately than average response times.

- **Alert name:** API Latency p95 High
- **Metric:**
  ```promql
  histogram_quantile(
    0.95,
    sum(rate(http_server_requests_seconds_bucket[5m])) by (le)
  )
  ```
- **Condition:** Fires when p95 latency exceeds a defined threshold for a sustained period
- **Rationale:** Focuses on tail latency to detect slowdowns before they escalate into outages
- **Auto-recovery:** Resolves automatically when latency returns to normal levels

#### Alert screenshot:

**Latency p95 – Normal:**  
![Latency Alert Normal](docs/alerts/latency-alert-normal.png)

---

### Why this alerting setup is production-grade

- **Clear separation** of concerns between availability, error rates, and performance metrics
- **Minimizes alert fatigue** through the use of pending windows and meaningful signals (5xx errors and p95 latency)
- **Aligns with real-world SRE/DevOps practices** for on-call monitoring and incident management
- **Suitable for portfolio demonstration** by showcasing mature monitoring and alerting capabilities

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
- ✔ Secure Prometheus metrics endpoint with token-based access (file-backed, non-expiring)

🔜 Next planned steps:
- CI/CD pipeline hardening
- API versioning (/api/v1)

---

## 👤 Author

**Víctor López**  
Software Engineer (BSc)  
Backend-focused – Java & Spring Boot

> This project is part of a larger backend-focused portfolio, oriented towards clean architecture, security and scalable system design.
