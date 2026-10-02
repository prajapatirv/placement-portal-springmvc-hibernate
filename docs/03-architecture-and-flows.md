# Task 5: Architecture and Flow Diagrams (with example flows)

Diagrams are Mermaid: they render on GitHub and in IntelliJ / VS Code (Markdown preview with Mermaid support).

## 1. Layered architecture

```mermaid
flowchart TB
    B[Browser / curl]
    subgraph Web["Web layer (Spring MVC)"]
        DS[DispatcherServlet<br/>front controller]
        C1[JobController<br/>CompanyController<br/>StudentController<br/>ApplicationController]
        C2[JobApiController<br/>@RestController, JSON]
        EH[GlobalExceptionHandler<br/>ApiExceptionHandler]
        V[Thymeleaf templates]
    end
    subgraph Svc["Service layer (@Transactional)"]
        S[JobService / CompanyService<br/>StudentService / ApplicationService]
        D[DTOs: JobView, ApplicationRow, Forms]
    end
    subgraph Data["Data layer"]
        R[Spring Data JPA repositories]
        H[Hibernate ORM / JPA]
        E[Entities: Company, JobPosting,<br/>Student, Application]
    end
    F[Flyway migrations V1 V2 V3]
    DB[(Supabase PostgreSQL<br/>schema app)]
    H2[(H2 in-memory<br/>local profile)]

    B --> DS --> C1 --> S
    DS --> C2 --> S
    S --> R --> H --> E
    H -->|JDBC| DB
    H -.->|local profile| H2
    F --> DB
    C1 --> V --> B
    C2 -->|JSON| B
    EH -.-> DS
```

Package-by-feature layout: `company/ job/ student/ application/ common/`; each feature has entity, repository, service, controller, forms and views together.

## 2. Request lifecycle (Demo 6)

```mermaid
sequenceDiagram
    actor U as Browser
    participant DS as DispatcherServlet
    participant HM as HandlerMapping
    participant C as JobController
    participant S as JobService (proxy @Transactional)
    participant R as JobRepository
    participant DB as PostgreSQL
    participant VR as Thymeleaf ViewResolver

    U->>DS: GET /jobs?city=Surat
    DS->>HM: getHandler
    HM-->>DS: JobController.list
    DS->>C: handle (binds city)
    C->>S: listOpenJobs("Surat")
    S->>R: findOpenByCity("Surat")
    R->>DB: select ... join fetch company where lower(city)=?
    DB-->>R: rows
    R-->>S: List of JobPosting
    S-->>C: List of JobView (DTO)
    C-->>DS: view name "jobs/list" + Model
    DS->>VR: render
    VR-->>U: HTML table
```

Key sentence: *the browser never talks to your controller; it talks to DispatcherServlet.*

## 3. Example flow A: apply for a job (POST, Post-Redirect-Get, validation, rules)

```mermaid
flowchart TD
    A[Student opens /jobs/4] --> B[GET /jobs/4/apply shows form]
    B --> C[POST /jobs/4/apply email=...]
    C --> D{Bean Validation<br/>@NotBlank @Email}
    D -- errors --> E[Redisplay apply.html<br/>with messages, HTTP 200]
    D -- ok --> F[ApplicationService.apply @Transactional]
    F --> G{job exists?}
    G -- no --> N404[NotFoundException -> 404 page]
    G -- yes --> H{open and not past last date?}
    H -- no --> N409a[JobClosedException -> 409 page]
    H -- yes --> I{student exists?}
    I -- no --> N404
    I -- yes --> J{already applied?<br/>existsByStudentIdAndJobId}
    J -- yes --> N409b[DuplicateApplicationException -> 409 page]
    J -- no --> K[save Application<br/>insert ... ; unique uq_application is the last guard]
    K --> L[flash message + redirect:/applications]
    L --> M[GET /applications shows list<br/>refresh does not re-submit]
```

Concrete run (H2 or Supabase): `POST /jobs/4/apply email=yash@ppsu.example` -> `302 /applications`; same request again -> `409 "This student has already applied to this job posting."`; `/jobs/2/apply` (QA Automation Trainee, CLOSED) -> `409 closed`; `/jobs/9999` -> `404`.

## 4. Example flow B: the N+1 demo (Demo 4)

```mermaid
flowchart LR
    subgraph Slow["GET /applications?slow=true  (listSlow)"]
        s1["1 select application"] --> s2["8 select student where id=?"]
        s2 --> s3["12 select job_posting where id=?"]
        s3 --> s4["5 select company where id=?"]
        s4 --> s5(["26 statements"])
    end
    subgraph Fast["GET /applications  (listFast)"]
        f1["1 select a, s, j, c<br/>from application a join student s join job_posting j join company c"] --> f2(["1 statement"])
    end
    subgraph Batch["profile batch (default_batch_fetch_size=20)"]
        b1["1 list + 1 students + 1 jobs + 1 companies"] --> b2(["4 statements"])
    end
```

## 5. Example flow C: stale form and optimistic locking (L5)

```mermaid
sequenceDiagram
    participant A as Tab A
    participant B as Tab B
    participant S as ApplicationService
    participant DB as application row (version 0)
    A->>S: GET status form (version=0)
    B->>S: GET status form (version=0)
    A->>S: POST status=SHORTLISTED, version=0
    S->>DB: update ... set status, version=1 where id=? and version=0
    DB-->>A: ok, redirect
    B->>S: POST status=REJECTED, version=0
    S-->>B: version 0 != 1 -> ObjectOptimisticLockingFailureException
    Note over B: GlobalExceptionHandler renders the 409 page
```

## 6. Example flow D: all-or-nothing bulk shortlist (transaction rollback)

```mermaid
flowchart LR
    X["bulkShortlist([1, 2, 9999])"] --> T[BEGIN]
    T --> a["id 1 -> SHORTLISTED (in memory)"]
    a --> b["id 2 -> SHORTLISTED (in memory)"]
    b --> c["id 9999 -> NotFoundException"]
    c --> R[ROLLBACK: no UPDATE reaches the database]
```

## 7. Environment / deployment view

```mermaid
flowchart LR
    Dev[Developer laptop<br/>JDK 21 + mvnw] -->|"config/supabase.properties"| App[Spring Boot app :8080]
    App -->|"JDBC, Session pooler :5432, sslmode=require"| SB[(Supabase PostgreSQL)]
    Dev2[No account / venue network blocks Supabase] -->|"-Dspring-boot.run.profiles=local"| App2[Same app + in-memory H2]
    CI[GitHub Actions] -->|"./mvnw verify, profile local"| App2
```
