## Test Strategy - secure-rest-api-demo

### Goal / Risk reduced

The goal of this test strategy is to ensure that the secure REST API behaves correctly, securely, and reliably under normal and abnormal conditions.

The main risks this strategy aims to reduce are:
- **Authentication and authorization failures**
- **Data integrity issues** caused by incorrect validation or database interactions.
- **API contract regressions** where endpoints return incorrect responses after code changes.
- __Security vulnerabilities__ such as accepting invalid tokens or exposing protected endpoints. 

---
### Scope 

**In this scope**

The following components will be tested:
- REST API endpoints
- Authentication and authorization mechanisms (JWT)
- Business logic in services
- Database interactions
- Error handling and validation
- Security behaviour of protected endpoints

**Out of scope**

The following aspects are not the focus of this strategy:
- Frontend applications
- Full browser-based end-to-end testing
- External infrastructure reliability (cloud provider uptime)

---
### Test levels (unit, integration, API, E2E)

**Unit tests**

Purpose: verify individual components in isolation.

Examples:
- Service layer logic
- Validation logic
- Utility methods

Characteristics:
- Fast execution
- No external dependencies
- High coverage expected

**Integration tests**

Purpose: verify interactions between components.

Examples:
- Controller -> service -> repository flow
- Database interaction using Testcontainers

These tests validate that the application works correctly with the real persistence layer.

**API tests:**

Purpose: validate REST endpoints from the outside.

Examples:
- Correct HTTP status codes
- Response payload validation
- Authentication and authorization behaviour

Typical checks:
- Valid token -> access allowed
- Invalid token -> 401
- Valid token but wrong role -> 403

**End-to-End tests**

Limited use.
Full system behaviour may be validated through integration/API tests instead of complex E2E scenarios.

---
### Environments

Testing will run in the following environments:

**Local environment**

Developers run tests locally using Maven and Docker.

**CI environment**

GitHub Actions executes tests automatically on each push.

**Containerized environment**

Docker Compose is used to run dependencies such as the database.

---

### Tooling

Testing tools used in the project:
- **JUnit** for unit and integration tests
- **Spring Boot Test**
- **Testcontainers** for database testing
- **Maven** as build system
- **GitHub Actions** for CI
- **Docker Compose** for environment setup

---

### Metrics

Quality will be monitored using the following metrics:
- **Test coverage**
- **Test execution time**
- **Build success rate**
- **Number of failing tests**
- **API response time for critical endpoints**

---

### Entry/Exit criteria

**Entry criteria**

Testing begins when:
- Code is implemented
- Application compiles successfully
- Required dependencies are available

**Exit criteria**

Testing is considered complete when:
- All tests pass in CI
- No critical defects remain
- Core API endpoints behave as expected

---

### Non-functional

Some non-functional aspects will also be considered:
- **Performance:** API responses should typically remain under 500ms.
- **Security behaviour:** unauthorized access must always be blocked.
- **Reliability:** system should not crash when invalid input is provided.

---

### Known gaps

Current gaps that may need improvement:
- Limited performance testing.
- No automated load testing yet.
- Security testing could be extended with more negative cases.
- Contract testing between services is not implemented.
