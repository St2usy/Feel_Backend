# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

FeeL Backend — Spring Boot REST API for 전북대학교 공과대학 (JBNU College of Engineering) student council. Manages notices, gallery, calendar, matching posts, finance reports, pledge progress, activity posts, and downloadable resource files, plus admin/Google auth.

- Java 17, Spring Boot 3.2.1, Spring Data JPA (Hibernate), Lombok, Maven
- H2 file DB for local dev, MySQL 8.0 for production (both configured; PostgreSQL driver also bundled but commented out)

## Commands

Use the Maven wrapper (`mvnw` on Unix/Git Bash, `mvnw.cmd` on Windows CMD).

```bash
./mvnw spring-boot:run        # run locally (default profile → H2 at ./data/feeldb)
./mvnw clean compile          # compile
./mvnw package                # build jar to target/
./mvnw package -DskipTests    # build without tests
./mvnw test                   # run tests
./mvnw test -Dtest=ClassName#methodName   # run a single test
```

There are currently **no test classes** under `src/test`. `./mvnw test` passes trivially.

Run with a specific profile:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
# or
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

H2 console (dev only): http://localhost:8080/h2-console — JDBC `jdbc:h2:file:./data/feeldb`, user `sa`, empty password.

## Configuration profiles

- `application.properties` (default/no profile): H2 file DB, H2 console on, `ddl-auto=update`, multipart uploads to `uploads/`.
- `application-dev.properties`: H2 with verbose SQL logging.
- `application-prod.properties`: MySQL via `jdbc:mysql://db:3306/feeldb`, credentials from `${DB_USERNAME}`/`${DB_PASSWORD}` env vars, `ddl-auto=update`, H2 console off, `app.base-url` from `${APP_BASE_URL}`.

`ddl-auto=update` in every profile — Hibernate auto-migrates the schema from entities; there are no migration scripts (Flyway/Liquibase not used).

## Architecture

Classic layered Spring MVC under `com.feel.backend`:

```
controller/  → REST endpoints; catch service RuntimeExceptions, map to HTTP status
service/     → business logic; @Transactional(readOnly=true) at class level, @Transactional on writes
repository/  → Spring Data JPA interfaces (derived queries + @Query)
entity/      → JPA entities (Lombok @Getter/@Setter/@Builder, @CreationTimestamp/@UpdateTimestamp)
dto/         → request/response DTOs; entities never serialized directly (fromEntity() converters)
config/      → CORS, security bean, request logging, admin seeding
validation/  → @ValidCategory custom bean-validation annotation + CategoryValidator
util/        → JwtUtil
```

Each feature domain follows the same controller→service→repository→entity+dto slice. Domains and their base paths:

| Domain | Base path |
|--------|-----------|
| Auth | `/api/auth` |
| Notice | `/api/notices` |
| Gallery | `/api/gallery` |
| Calendar | `/api/calendar/events` |
| Matching | `/api/matching` |
| Finance report | `/api/finance/reports` |
| Pledge progress | `/api/pledges` |
| Activity post | `/api/activities` |
| Resource file | `/api/resources` |

### Conventions to follow when adding/changing code

- **Error handling**: services throw plain `RuntimeException(message)`. Controllers wrap calls in try/catch and build an `ErrorResponse` with the appropriate `HttpStatus` (there is no global `@ControllerAdvice`). Match this pattern rather than introducing custom exceptions unless refactoring the whole layer.
- **DTO conversion** happens in DTOs via static `fromEntity(...)` (and nested `Request`/`Response` DTO classes in newer domains like `ResourceFileDto`).
- **Transactions**: annotate write methods with `@Transactional`; read services are `@Transactional(readOnly=true)` at class level.
- **Constructor injection** via Lombok `@RequiredArgsConstructor` on `final` fields — do not add `@Autowired` field injection.
- **View counts**: entities expose `incrementViewCount()`, called from single-item GET services.

### Auth (important, non-obvious)

- `spring-boot-starter-security` is **not** on the classpath. `SecurityConfig` only exposes a `BCryptPasswordEncoder` bean — there is **no security filter chain, so no route is protected by the framework**. Endpoints that need auth check the `Authorization: Bearer <jwt>` header manually (see `AuthController.verify`). Adding real route protection means wiring up Spring Security and, per the note in `SecurityConfig`, permitting `OPTIONS /**` for CORS preflight.
- Three login paths in `AuthService`: (1) admin username/password (BCrypt), (2) Google OAuth via `GoogleTokenVerifier` (restricted to `@jbnu.ac.kr` emails; new users go through `/api/auth/signup` to set a nickname), (3) JWT issued by `JwtUtil` (HS256, secret + expiry from `jwt.secret`/`jwt.expiration`, defaulted in code — override in prod). JWT is stateless; `logout` is a no-op.
- `DataInitializer` (CommandLineRunner) seeds/repairs an `admin` / `admin123` account on every startup.

### File uploads & static serving

- `FileStorageService` stores multipart files under the `file.upload-dir` (`uploads/`) with UUID-based names; max 10MB. Notices treat images as optional, gallery/resources require them.
- `WebConfig` serves stored files as static resources at `/uploads/**` from the absolute upload path, and defines the CORS policy.

### CORS

Allowed origins are a hardcoded list in `WebConfig.addCorsMappings` (jbnu.ac.kr subdomains, feel-test.com, localhost dev ports, nip.io test hosts), for both `/api/**` and `/uploads/**`, with `allowCredentials(true)`. **Add new frontend origins to both mappings in `WebConfig.java`** — controllers do not use `@CrossOrigin`.

## Deployment

Production runs on a GCP VM via Docker + Nginx (reverse proxy, ports 80/443, SSL via Certbot) with a MySQL container. The `Dockerfile` is a multi-stage Maven build; note it currently uses the **local-dev base images** (`eclipse-temurin:17-jre` + `apt-get`) — the production Alpine variants are commented out. `nginx/` holds the proxy config.

**There is no `docker-compose.yml` in this repo**, although `README.md` and `md/DEPLOY.md` reference `docker-compose` commands — the compose file lives on the deployment server, not in version control.

## Further docs

The `md/` directory contains detailed Korean-language references: `ARCHITECTURE.md`, `API_SPECIFICATION.md`, `BACKEND_AUTH_API.md`, `CALENDAR_API.md`, `CURL_TEST_COMMANDS.md`, `DEPLOY.md`. Note `md/CLAUDE.md` is an older, Notice/Gallery-only version superseded by this file.
