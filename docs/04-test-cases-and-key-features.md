# Task 6: Test Cases and Key Feature Highlights

## 1. How to run

```bash
./mvnw verify                      # all tests on in-memory H2 (no Supabase needed); Windows: mvnw.cmd verify
./mvnw test -Dtest=QueryCountTest  # one class
```

Last verified run: **38 tests, 0 failures** (Java 21, Spring Boot 3.5.16). Each test class uses its own in-memory H2 database, so classes cannot disturb each other's data.

## 2. Automated test inventory

| Class | Tests | Type | What it proves |
| --- | --- | --- | --- |
| `QueryCountTest` | 2 | `@SpringBootTest` + Hibernate Statistics | `listSlow()` = **26** JDBC statements, `listFast()` = **1** |
| `BatchFetchQueryCountTest` | 1 | profile `batch` | batch fetching drops the slow path to **4** statements |
| `ApplicationRepositoryTest` | 7 | `@DataJpaTest` | Flyway V1 to V3 apply on H2, `ddl-auto=validate` passes, seed counts 5/12/8/20, derived query `existsBy...`, JOIN FETCH, case-insensitive city filter, paging with `@EntityGraph`, two-column projection |
| `ApplicationServiceTest` | 6 | service + transactions | apply, duplicate, closed job, unknown ids, dirty checking, rollback of bulk shortlist, stale-version rejection |
| `JobControllerTest` | 12 | MockMvc through the full stack | pages, filter, detail, 404 page, JSON, JSON 404, validation message, PRG redirect, 409 pages, paging, slow/fast toggle, home |
| `CrudFlowTest` | 10 | MockMvc | create / edit / delete of Company, Student, JobPosting; validation; duplicates; delete guards; stale status form; withdraw |

## 3. Manual test cases (use for the live demo and for QA)

Start with `./mvnw spring-boot:run -Dspring-boot.run.profiles=local` and open `http://localhost:8080`.

| ID | Area | Steps | Expected |
| --- | --- | --- | --- |
| TC-01 | Startup | run the app | Flyway reports 3 migrations applied; Hibernate validate passes; home shows 5 / 12 / 8 / 20 |
| TC-02 | Read | open `/jobs` | 11 open postings; "QA Automation Trainee" (CLOSED) absent; sorted by last date |
| TC-03 | Filter | `/jobs?city=surat` | 3 postings (Gujarat Cloudworks); console shows exactly one `select` |
| TC-04 | Detail | click a title | detail with company, city, package; one SQL join |
| TC-05 | JSON | `/api/jobs`, `/api/jobs/1`, `/api/jobs/summaries` | JSON; `/api/jobs/9999` returns JSON problem with status 404 |
| TC-06 | 404 | `/jobs/9999` | friendly 404 page, no stack trace |
| TC-07 | Validation | apply with empty email | form redisplays, "Email is required", HTTP 200 |
| TC-08 | Apply | apply `yash@ppsu.example` to "Data Analyst Intern" | redirect to `/applications`, message "Application submitted" |
| TC-09 | Duplicate | repeat TC-08 | 409 page "already applied" |
| TC-10 | Closed job | apply to "QA Automation Trainee" | 409 page "closed" |
| TC-11 | Unknown student | apply `nobody@ppsu.example` | 404 page |
| TC-12 | N+1 slow | `/applications?slow=true`, count `select` lines in console | 26 statements; Session Metrics shows 26 |
| TC-13 | N+1 fast | `/applications` | 1 statement |
| TC-14 | N+1 runner | run with `--demo.n-plus-one=true` | console prints SLOW block then FAST block |
| TC-15 | Batch fix | profiles `local,batch`, TC-12 | 4 statements |
| TC-16 | Dirty checking | click **Shortlist** on a row | console: `update application set ... where id=? and version=?`; no `save()` in code |
| TC-17 | Rollback | tick two rows, add a non-existing id (edit the form or use curl `ids=1&ids=2&ids=9999`) | 404 page; rows 1 and 2 unchanged |
| TC-18 | Optimistic lock | open **Change status** for one row in two tabs; save A, then save B | A succeeds; B shows 409 |
| TC-19 | Withdraw | **Withdraw** on a row | row disappears, message shown |
| TC-20 | Company CRUD | add, edit, delete a new company | each shows a message; list updates |
| TC-21 | Company guards | delete "Nimbus Tech"; add a company named "nimbus tech" | blocked: has 2 postings; duplicate-name error on the form |
| TC-22 | Student CRUD | add student (email upper case, branch lower case), edit, delete | email stored lower case, branch upper case |
| TC-23 | Student validation | email `abc`, CGPA 11 | both field errors shown |
| TC-24 | Student guard | delete "Asha Patel" | blocked: 3 applications exist |
| TC-25 | Job CRUD | add, edit (status CLOSED), delete | closed job disappears from `/jobs`, still on `/jobs/manage` |
| TC-26 | Job rules | max package below min; duplicate title; no company | three different error messages; nothing saved |
| TC-27 | Paging | `/jobs/manage`, Next / Previous | 5 rows per page, "Page 1 of 3" |
| TC-28 | Health | `/actuator/health` | `{"status":"UP"}` |
| TC-29 | Supabase | fill `config/supabase.properties`, run `start.cmd` (Supabase mode) | tables appear in Supabase Table Editor, schema `app`, plus `flyway_schema_history` |
| TC-30 | Connection errors | wrong password / transaction pooler 6543 | matches the "Common errors" table in the README |

## 4. Key features to highlight

| # | Feature | Where | One-line message |
| --- | --- | --- | --- |
| 1 | Annotation to SQL | `Company`, `JobPosting`, demo 1 | annotations are data that Hibernate reads by reflection |
| 2 | LAZY by default | `@ManyToOne(fetch = LAZY)` | proxies defer the query until you touch the object |
| 3 | N+1 made visible | `QueryCountTest`, `/applications?slow=true` | 26 -> 1 (JOIN FETCH) or 4 (batch) |
| 4 | Derived queries | `existsByStudentIdAndJobId` | no implementation to write |
| 5 | DTO boundary | `JobView`, `ApplicationRow`, forms | entities never reach the template |
| 6 | One Spring MVC flow, two outputs | `JobController` + `JobApiController` | same service, HTML or JSON |
| 7 | Validation + PRG | `ApplyForm`, `apply` method | errors redisplay, success redirects |
| 8 | Global error handling | `GlobalExceptionHandler`, `ApiExceptionHandler` | 404 / 409 pages and JSON, never a stack trace |
| 9 | Transactions at the service layer | `@Transactional` everywhere in services | proxy begins and commits (demo 2) |
| 10 | Dirty checking | `shortlist`, `update` methods | no `save()` yet the UPDATE appears |
| 11 | Rollback | `bulkShortlist` | all or nothing |
| 12 | Optimistic locking | `@Version`, `StatusForm` | stale browser forms are detected |
| 13 | Schema as code | Flyway + `ddl-auto=validate` | DB changes are versioned; mapping mismatch fails at startup |
| 14 | Two databases, one code | profile `local` (H2) vs Supabase | no student is blocked by accounts or networks |
| 15 | Performance toolbox | paging, `@EntityGraph`, projection, batch size, index | pick the lightest fix that works |
| 16 | Secrets hygiene | `config/supabase.properties` (git-ignored), `.gitignore` | the password never enters Git |
