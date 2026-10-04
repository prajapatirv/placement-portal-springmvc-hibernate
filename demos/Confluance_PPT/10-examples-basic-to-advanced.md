# Examples from basic to advanced (Hibernate and Spring MVC)

Companion to the session deck (`PPSU_Part1_Hibernate_SpringMVC_v2.pptx`) and to `docs/06` (live script). Every example uses the Placement Portal you already have, so students see **one app growing**, not ten unrelated demos.

**How to read an example**

| Label | Meaning |
| --- | --- |
| **IN REPO** | The code already exists. Open the file, run the page, read the SQL log. |
| **ADD** | A small piece of new code. Not in the repo yet. Add it live, or pre-load it on a branch. |
| **PREDICT** | Ask the room first. Hands up for each option. Then reveal. |
| **BREAK IT** | Change one thing on purpose, watch it fail, then fix it. Students remember failures. |

> **Before the session:** the `ADD` snippets below were written against the current code but were **not compiled or run** when this file was prepared. Paste each one into a scratch branch, run `./mvnw verify`, and fix any import or typo before you show it live. Each `ADD` has a check at the end (a test or a `curl`) so you know it works.

**Level map**

| Level | Hibernate | Spring MVC |
| --- | --- | --- |
| 1 Basics | H1 Mapping, H2 CRUD | M1 Controllers and parameters |
| 2 Intermediate | H3 Relationships, H4 Queries | M2 Model and views, M3 Forms and validation |
| 3 Production | H5 Fetching and N+1, H6 Persistence context, H7 Transactions and locking | M4 REST, M5 Errors, M6 Interceptor |
| 4 Advanced (optional) | H8 Bulk operations | M7 Layers and DTO boundary, M8 Where to go next |

Cheat-sheet rule for the whole day: **every click in the browser must be answered by a line in the console.**

---

# Part A. Hibernate

## H1. Map a class to a table (Level 1)

**Real-life link:** a student record in the university system is one row. The Java object is the same record in memory.

**IN REPO:** `student/Student.java`, `db/migration/V1__init.sql`, `demos/Demo1Annotations.java`.

**PREDICT:** In `Student`, change `@Column(nullable = false) private String branch;` to `@Column(name = "department", nullable = false)`. Start the app. What happens?
- A) It starts and creates a new column
- B) It starts and ignores the change
- C) It refuses to start

**Reveal:** C. Flyway built the table, Hibernate runs in `validate` mode, and `department` does not exist in `student`, so startup fails with a schema-validation error. That is the point: the mapping and the schema cannot drift apart silently. Put the annotation back.

**Enum mapping (IN REPO):** `JobPosting.status` uses `@Enumerated(EnumType.STRING)`.
- **Why STRING:** the column holds `OPEN` or `CLOSED`, readable in any SQL tool.
- **BREAK IT (thought experiment, do not run):** with `ORDINAL` the column holds 0 and 1. Insert a new value in the middle of the enum next year and every old row silently changes meaning.

**Interview line:** "Why `EnumType.STRING`?" Because the stored value stays stable when the enum is reordered.

---

## H2. CRUD without writing SQL (Level 1)

**Real-life link:** every admin screen in every college portal: list, add, edit, delete.

**IN REPO:** `student/StudentRepository.java` (`findByEmail`, `existsByEmailIgnoreCase`), page `/students`.

**Run:** open `/students`, add a student, edit, delete. After each click, point at the console: `insert`, `update`, `delete`.

**ADD (derived queries, the method name is the query):**

```java
// StudentRepository.java
List<Student> findByBranchIgnoreCaseAndCgpaGreaterThanEqualOrderByCgpaDesc(String branch, BigDecimal minCgpa);
```

**Check (test):**

```java
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:ex1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS app")
@ActiveProfiles("local")
class StudentQueriesTest {
    @Autowired StudentRepository students;

    @Test
    void ce_students_with_cgpa_at_least_8_best_first() {
        var found = students.findByBranchIgnoreCaseAndCgpaGreaterThanEqualOrderByCgpaDesc("ce", new BigDecimal("8.0"));
        // seed data: Meera 9.1, Asha 8.6, Nisha 8.0 (all CE)
        assertThat(found).extracting(Student::getName).containsExactly("Meera Desai", "Asha Patel", "Nisha Parmar");
    }
}
```

**Say:** "That method has no body. Spring reads the name: branch equals, cgpa at least, order by cgpa descending." Read the generated `select ... where upper(branch)=upper(?) and cgpa>=? order by cgpa desc` in the log.

---

## H3. Relationships: companies, jobs, applications (Level 2)

**Real-life link:** a food-delivery app. One restaurant has many dishes. Many customers order many dishes. The **order** is its own thing and carries a status and a time. Here the order slip is `Application`.

**IN REPO:** `Company` (`@OneToMany(mappedBy = "company")`), `JobPosting` (`@ManyToOne` owning side), `Application` (association entity with `status`, `appliedAt`, `@Version`). Supabase Table Editor, schema `app`: 5 companies, 12 jobs, 8 students, 20 applications.

**PREDICT:** Which table holds the foreign key between company and job? Answer: `job_posting.company_id`. So `JobPosting` is the **owning side**, and `Company.postings` only says `mappedBy = "company"`.

**ADD ("My applications": naive version first, to create a fresh N+1):**

```java
// ApplicationRepository.java  (naive: no fetch plan)
List<Application> findByStudentEmailIgnoreCase(String email);

// ApplicationService.java
@Transactional(readOnly = true)
public List<ApplicationRow> mineSlow(String email) {
    return applications.findByStudentEmailIgnoreCase(email).stream().map(this::toRow).toList();
}

// ApplicationController.java
@GetMapping("/applications/mine")
public String mine(@RequestParam String email, Model model) {
    model.addAttribute("rows", applications.mineSlow(email));
    model.addAttribute("slow", false);
    return "applications/list";          // reuse the existing template
}
```

Open `/applications/mine?email=asha@ppsu.example` and count the `select` lines (Asha has three applications). It is more than one. **Same bug as the 101 query page, found in code the students just wrote.** Fix it in H5.

---

## H4. Three ways to ask the database (Level 2)

**IN REPO:** `JobRepository` has all three.

| Style | Example in repo | Use it when |
| --- | --- | --- |
| Derived query | `existsByTitleIgnoreCase`, `countByCompanyId` | the question fits in a method name |
| JPQL with `@Query` | `findOpenByCity` (`join fetch`) | joins, filters, ordering you must control |
| Projection | `summaries()` returning `JobSummary` | you need two columns, not the whole entity |

Live: `/jobs?city=Surat` is one `select` with a join. `/api/jobs/summaries` selects only title and company name.

**ADD (aggregate projection: how many applicants per job, including jobs with zero):**

```java
// job/JobApplicantCount.java
public interface JobApplicantCount {
    String getTitle();
    Long getApplicants();
}

// JobRepository.java
@Query("""
        select j.title as title, count(a) as applicants
        from JobPosting j left join Application a on a.job = j
        group by j.title
        order by count(a) desc, j.title
        """)
List<JobApplicantCount> applicantsPerJob();

// JobApiController.java
@GetMapping("/popular")
List<JobApplicantCount> popular() { return jobs.applicantsPerJob(); }
```
(`JobService` needs a small method that calls `jobs.applicantsPerJob()` inside `@Transactional(readOnly = true)`, like `summaries()` does.)

**PREDICT:** If we used `from Application a group by a.job.title` instead, which jobs disappear? Answer: jobs with **zero applicants**, because there is no application row to group. The `left join` keeps them with count 0.

**Check:** `curl localhost:8080/api/jobs/popular` shows the busiest job first. Cross-check in SQL: `select job_id, count(*) from app.application group by job_id order by 2 desc;`

**Native SQL:** exists (`nativeQuery = true`). Mention it, do not use it today. It ties you to one database and to the schema name.

---

## H5. LAZY, EAGER and the N+1 problem (Level 3)

**Real-life link:** the page that asked the database 101 times.

**IN REPO:** `ApplicationService.listSlow()` (26 statements), `listFast()` (1, `join fetch`), profile `batch` (4), tests `QueryCountTest` and `BatchFetchQueryCountTest`, `/applications?slow=true` vs `/applications`.

| Version | Statements | How |
| --- | --- | --- |
| LAZY loop | 26 | 1 list + 8 students + 12 jobs + 5 companies |
| Batch fetch size 20 | 4 | `hibernate.default_batch_fetch_size=20`, profile `local,batch` |
| `JOIN FETCH` | 1 | `findAllWithDetails()` |

**PREDICT:** 20 applications on one page: 1, 5, 26 or 100 queries? (Answer 26.)

**BREAK IT 1:** remove `@Transactional` from `listSlow()` and open `/applications?slow=true`. Result: `LazyInitializationException`. `open-in-view=false` is on purpose, so students meet the exception here instead of in production.

**BREAK IT 2 (discussion, keep it short):** switch `Application.student` to `EAGER`. The page still fires extra selects, and now **every** page that loads an application pays for the student, used or not. EAGER does not fix N+1.

**ADD (fix the "My applications" page from H3 with an entity graph):**

```java
// ApplicationRepository.java
@EntityGraph(attributePaths = {"student", "job", "job.company"})
List<Application> findByStudentEmailIgnoreCase(String email);   // replaces the naive version
```

**Check (test, pattern copied from `QueryCountTest`):**

```java
@Test
void mine_is_one_statement_with_the_entity_graph() {
    stats.clear();
    assertThat(service.mineSlow("asha@ppsu.example")).isNotEmpty();
    assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
}
```
Run it first against the naive repository method and show it fail (more than 1), then add the `@EntityGraph` and show it pass. This is the best 3 minutes of the Hibernate part.

---

## H6. The persistence context: lifecycle, dirty checking, one object per row (Level 3)

**Real-life link:** the attendance register. Pencil (new), ink (being tracked), left the room (detached), struck off (removed).

**IN REPO:** `ApplicationService.shortlist()` has no `save()`; the row still updates (dirty checking). Button **Shortlist** on `/applications`.

**PREDICT:** "No `save()` call. Will the row update?" (Yes: `update application set status=?, version=? where id=? and version=?`.)

**ADD (the four states in one test):**

```java
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:ex2;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS app")
@ActiveProfiles("local")
@Transactional
class EntityLifecycleTest {
    @PersistenceContext EntityManager em;

    @Test
    void transient_persistent_detached_removed() {
        Student s = new Student("Lifecycle Test", "life@ppsu.example", "CE", new BigDecimal("8.0"));
        assertThat(em.contains(s)).isFalse();            // TRANSIENT: Hibernate does not know it

        em.persist(s);
        assertThat(em.contains(s)).isTrue();             // PERSISTENT: tracked
        assertThat(s.getId()).isNotNull();               // IDENTITY: the insert ran immediately

        em.detach(s);
        assertThat(em.contains(s)).isFalse();            // DETACHED: no longer tracked
        s.setName("Changed while detached");             // nobody sees this change

        Student reloaded = em.find(Student.class, s.getId());
        assertThat(reloaded.getName()).isEqualTo("Lifecycle Test");

        em.remove(reloaded);
        assertThat(em.contains(reloaded)).isFalse();     // REMOVED: delete at commit
    }
}
```

**ADD (first-level cache, one object per row per session):**

```java
@Test
void same_id_twice_in_one_transaction_is_one_select_and_one_object() {
    Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();   // inject EntityManagerFactory emf
    stats.clear();
    var first = applications.findById(1L).orElseThrow();
    var second = applications.findById(1L).orElseThrow();
    assertThat(second).isSameAs(first);
    assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
}
```
**Say:** "This is the first-level cache. It lives inside one session and you cannot turn it off. We are not covering the second-level cache today."

---

## H7. Transactions and optimistic locking (Level 3)

**Real-life link:** a bank transfer (debit and credit together or not at all), and two admins editing the same row in two browser tabs.

**IN REPO:** `bulkShortlist(List<Long>)` is `@Transactional`; `changeStatus(StatusForm)` checks `@Version`. Tests in `ApplicationServiceTest`.

**Live (rollback):**
```
curl -i -X POST localhost:8080/applications/bulk-shortlist -d "ids=1&ids=2&ids=9999"
```
Expect `404`, and rows 1 and 2 **unchanged**: the exception rolled back the changes made earlier in the loop.

**Live (stale form):** open `/applications/1/status` in two tabs. Save in tab A (version 0 to 1). Save in tab B: **409**, "someone changed this before you". The SQL is `update ... where id=? and version=?`: zero rows matched.

**Say:** "Transactions protect consistency inside one request. Versions protect it between two humans."

---

## H8. Bulk operations: one UPDATE instead of N (Level 4, optional)

**Real-life link:** result day. The placement cell shortlists 500 applications at once.

**IN REPO:** `bulkShortlist` loops: for each id, one `select` and one `update` (about 2N statements).

**ADD (compare the two):**

```java
// ApplicationRepository.java
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("update Application a set a.status = :status where a.id in :ids")
int updateStatusIn(@Param("ids") Collection<Long> ids, @Param("status") ApplicationStatus status);

// ApplicationService.java
@Transactional
public int bulkShortlistFast(List<Long> ids) {
    return applications.updateStatusIn(ids, ApplicationStatus.SHORTLISTED);
}
```
**Check:** use the statistics pattern from H5 on both methods with ids `[1, 2, 3]`. Expect the loop version to fire several statements and the bulk version exactly one.

**The catch (this is the advanced lesson):**
- A bulk `update` bypasses the persistence context, so entities already loaded do not see the change (hence `clearAutomatically`).
- It also **skips `@Version` bookkeeping**: the version column is not incremented, so optimistic locking no longer protects those rows. In Hibernate 6 look at `update versioned ...` or increment the version yourself, and test it before you rely on it.
- It also skips entity callbacks and your business rules. Use it for large, simple, trusted changes only.

**Talking point (not a demo):** JDBC insert batching (`hibernate.jdbc.batch_size`) does **not** work with `GenerationType.IDENTITY`, which this project uses, because Hibernate must run each insert to learn the new id. Real batch inserts use a sequence generator. Mention this in one sentence if a student asks.

---

# Part B. Spring MVC

## M1. Controllers and request parameters (Level 1)

**Real-life link:** every URL you type or tap is a method call on a server.

**IN REPO:** `JobController`: `@GetMapping`, `@RequestParam(required = false) String city`, `@PathVariable Long id`. Try `/jobs`, `/jobs?city=Surat`, `/jobs/7`.

**ADD (a numeric parameter with automatic conversion):**

```java
// JobRepository.java
@EntityGraph(attributePaths = "company")
List<JobPosting> findByMinPackageLpaGreaterThanEqualOrderByMinPackageLpaDesc(BigDecimal minLpa);

// JobService.java
@Transactional(readOnly = true)
public List<JobView> paying(BigDecimal minLpa) {
    return jobs.findByMinPackageLpaGreaterThanEqualOrderByMinPackageLpaDesc(minLpa).stream().map(this::toView).toList();
}

// JobApiController.java
@GetMapping("/paying")
List<JobView> paying(@RequestParam BigDecimal minLpa) { return jobs.paying(minLpa); }
```

**Try and watch the status code:**

| URL | Expected |
| --- | --- |
| `/api/jobs/paying?minLpa=6` | 200, JSON list, best package first |
| `/api/jobs/paying` | 400, the required parameter is missing |
| `/api/jobs/paying?minLpa=abc` | 400, "abc" is not a number |

**Say:** "Spring converted the text `6` into a `BigDecimal` for us. When it cannot, the user gets a 400, not a stack trace."

---

## M2. Model, view name, and a template that stays simple (Level 2)

**IN REPO:** `JobController.list` puts `jobs` in the `Model` and returns `"jobs/list"`; Thymeleaf renders `templates/jobs/list.html` (`th:each`, `th:text`, `th:href`). The page receives `JobView` records, never entities.

**ADD (a "Closing soon" badge, with the rule kept out of the template):**

```java
// JobView.java: add a method to the record
public boolean closingSoon() {
    return !lastDate.isBefore(LocalDate.now()) && !lastDate.isAfter(LocalDate.now().plusDays(3));
}
```
```html
<!-- jobs/list.html, inside the Title cell, after the link -->
<span class="muted" th:if="${j.closingSoon()}"> (closing soon)</span>
```
**Check:** edit one posting's last date to tomorrow on `/jobs/{id}/edit`, then reload `/jobs`.

**Say:** "A template should only display. The decision 'closing soon' lives in Java where we can test it."

---

## M3. Forms and validation: never trust the browser (Level 2)

**Real-life link:** the guard at the gate checks your ID card so bad data never reaches the store.

**IN REPO:** `ApplyForm` (`@NotBlank`, `@Email`), `ApplicationController.apply` (`@Valid`, `BindingResult`, Post-Redirect-Get).

**PREDICT:** Submit an empty email. What comes back? Answer: the same form with "Email is required", status 200 (it is a page, not an API).

**BREAK IT (bypass the browser):**
```
curl -i -X POST localhost:8080/jobs/1/apply -d "email="
```
The HTML5 `required` attribute does not exist in curl, yet the server still rejects it. Client-side checks are for convenience; server-side checks are for safety.

**Then the happy path:** a valid email returns **302** to `/applications` with a flash message. Press F5: nothing is submitted twice. That is Post-Redirect-Get.

**ADD (optional, shows a custom rule; remove after the demo because real students use other domains):**
```java
public record ApplyForm(
    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    @Pattern(regexp = ".+@ppsu\\.example$", message = "Use your university email")
    String email) {}
```

---

## M4. REST: the same data as JSON, and a proper 201 (Level 3)

**Real-life link:** the portal's mobile app does not want HTML. It wants the same data as JSON.

**IN REPO:** `JobApiController` (`@RestController`), `/api/jobs`, `/api/jobs/{id}`, `/api/jobs/summaries`.

**ADD (a write endpoint that returns `201 Created` and a `Location` header):**

```java
// application/ApplicationApiController.java
@RestController
@RequestMapping("/api/applications")
class ApplicationApiController {
    private final ApplicationService applications;

    ApplicationApiController(ApplicationService applications) {
        this.applications = applications;
    }

    record ApplyRequest(@NotNull Long jobId, @NotBlank @Email String email) {}

    @PostMapping
    ResponseEntity<ApplicationRow> apply(@Valid @RequestBody ApplyRequest request) {
        Application saved = applications.apply(request.jobId(), request.email());
        ApplicationRow row = applications.get(saved.getId());
        return ResponseEntity.created(URI.create("/api/applications/" + saved.getId())).body(row);
    }

    @GetMapping("/{id}")
    ApplicationRow one(@PathVariable Long id) {
        return applications.get(id);
    }
}
```

**Try:**
```
curl -i -X POST localhost:8080/api/applications \
     -H "Content-Type: application/json" \
     -d '{"jobId":1,"email":"isha@ppsu.example"}'
```

| Case | Status |
| --- | --- |
| New application | 201 with a `Location` header (if the seed already has it, you get 409 instead) |
| Same request again | 409, already applied |
| Unknown email | 404 |
| Closed job | 409 |
| Missing field | 400 |

**Say:** "Same service as the HTML form. Different output. One business rule, two doors."

---

## M5. Errors: one handler for every failure (Level 3)

**IN REPO:** `GlobalExceptionHandler` (`@ControllerAdvice`, HTML pages) and `ApiExceptionHandler` (`@RestControllerAdvice`, JSON `ProblemDetail`).

**Reproduce each one live (use as a checklist):**

| Trigger | Status | Handled in |
| --- | --- | --- |
| open `/jobs/9999` | 404 page | `GlobalExceptionHandler.notFound` |
| open `/api/jobs/9999` | 404 JSON | `ApiExceptionHandler.notFound` |
| apply twice with the same email | 409 | `DuplicateApplicationException` (a `ConflictException`) |
| delete a job that has applications | 409 message | `JobService.delete` |
| stale status form in two tabs | 409 | `ObjectOptimisticLockingFailureException` |
| empty email in the form | 200, form again | `BindingResult` in the controller |

**Say:** "Same exception, two renderings: a friendly page for a person, JSON for a program. The user never sees a stack trace."

**ADD (field-level errors for `@Valid @RequestBody`, so M4 returns a clear 400):**

```java
// ApiExceptionHandler.java
@ExceptionHandler(MethodArgumentNotValidException.class)
ProblemDetail invalid(MethodArgumentNotValidException e) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
    Map<String, String> errors = new LinkedHashMap<>();
    e.getBindingResult().getFieldErrors().forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));
    problem.setProperty("errors", errors);
    return problem;
}
```
**Check:** `curl -i -X POST localhost:8080/api/applications -H "Content-Type: application/json" -d '{}'` returns 400 with an `errors` object listing `jobId` and `email`.

---

## M6. A HandlerInterceptor that prints the SQL count of every request (Level 3, the showpiece)

**Why this one matters:** it joins both halves of the day. Hibernate decides how often we ask the database. Spring MVC decides what the user sees. This interceptor prints, for **every** request, how many SQL statements it caused. The N+1 problem becomes visible on every page.

**ADD:**

```java
// common/QueryCountInterceptor.java
@Component
class QueryCountInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(QueryCountInterceptor.class);
    private final Statistics stats;

    QueryCountInterceptor(EntityManagerFactory emf) {
        this.stats = emf.unwrap(SessionFactory.class).getStatistics();
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        req.setAttribute("qc.sql", stats.getPrepareStatementCount());
        req.setAttribute("qc.start", System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        Long sqlBefore = (Long) req.getAttribute("qc.sql");
        Long start = (Long) req.getAttribute("qc.start");
        if (sqlBefore == null || start == null) return;
        long sql = stats.getPrepareStatementCount() - sqlBefore;
        long ms = (System.nanoTime() - start) / 1_000_000;
        log.info("{} {} -> {} | {} SQL | {} ms", req.getMethod(), req.getRequestURI(), res.getStatus(), sql, ms);
    }
}

// common/WebConfig.java
@Configuration
class WebConfig implements WebMvcConfigurer {
    private final QueryCountInterceptor queryCount;

    WebConfig(QueryCountInterceptor queryCount) {
        this.queryCount = queryCount;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(queryCount).excludePathPatterns("/css/**", "/actuator/**");
    }
}
```
Imports: `jakarta.servlet.http.HttpServletRequest`, `jakarta.servlet.http.HttpServletResponse`, `jakarta.persistence.EntityManagerFactory`, `org.hibernate.SessionFactory`, `org.hibernate.stat.Statistics`, `org.slf4j.Logger`, `org.slf4j.LoggerFactory`, `org.springframework.web.servlet.HandlerInterceptor`, `org.springframework.web.servlet.config.annotation.*`. It relies on `hibernate.generate_statistics=true`, which `application.properties` already sets.

**Run and read the console:**
```
GET /applications?slow=true -> 200 | 26 SQL | 48 ms
GET /applications           -> 200 | 1 SQL  | 9 ms
```
(Times will differ; the SQL counts match `QueryCountTest`.)

**Say:** "A filter sits at the servlet level and knows nothing about Spring. An interceptor sits around the controller call. `@ControllerAdvice` handles exceptions. Three tools, three jobs."

**Honest limit:** the counter is global to the application, so with several users at once the numbers mix. It is a teaching tool, not production monitoring.

---

## M7. Layers and the DTO boundary (Level 4, optional)

**IN REPO:** controller to service to repository; `JobView`, `ApplicationRow`, `CompanyView` are flat records. Controllers and templates never see entities.

**BREAK IT (then revert):** add a throwaway endpoint that returns an entity directly.
```java
@GetMapping("/raw")
List<JobPosting> raw() { return jobs.rawEntities(); }   // calls jobRepository.findAll()
```
Expect a 500 error from JSON serialization (a lazy `company` proxy outside a session, and `Company.postings` pointing back at jobs). Delete the endpoint.

**Say:** "Returning entities leaks your table design, fails on lazy proxies, and can recurse forever. The DTO is the plated dish; the entity is the kitchen container."

---

## M8. Where to go next (Level 4)

Today is the skeleton. Real projects add these. None are in the repo yet, so say "next step", not "we did it".

| Topic | First step |
| --- | --- |
| Spring Security | login page and roles (student, placement officer); `spring-boot-starter-security` |
| Async requests | return `Callable` or `DeferredResult` from a controller for slow work |
| Tests | already in repo: MockMvc, DataJpaTest, query-count tests; add one per new endpoint |
| Monitoring | Actuator health is on; add metrics later |
| Deployment | Docker image, then CI (`.github/workflows/build.yml` exists) |

Free learning links are in `docs/09-resources.md`.

---

# Part C. Challenge board

| Level | Task | Done when |
| --- | --- | --- |
| Easy | `/students?branch=CE` using the H2 derived query | list shows only CE students, one `select` |
| Easy | "Closing soon" badge (M2) | badge appears for a job ending within 3 days |
| Medium | "My applications" with one statement (H3 + H5) | `mineSlow` test passes with exactly 1 statement |
| Medium | Applicants per job (H4) | `/api/jobs/popular` lists jobs with 0 applicants too |
| Hard | Query-count interceptor (M6) | each request logs method, URL, status, SQL count |
| Hard | Bulk shortlist in one UPDATE (H8) | statistics show 1 statement for the bulk method |
| Stretch | Add field errors for `@Valid @RequestBody` (M5) | `{}` returns 400 with an `errors` object |

Existing repo challenge (city filter, one query): keep it as the 7-minute challenge on the checkpoint slide. The finished solution (`findOpenByCity`) is already in `main`, so the starting point has to be a tag where that method is missing.

---

# Part D. Prediction cards (hands up, then reveal)

| # | Question | Options | Answer |
| --- | --- | --- | --- |
| 1 | Rename the `branch` column mapping and start the app | creates column / ignores / refuses to start | refuses to start (validate) |
| 2 | 20 applications on one page, LAZY loop | 1 / 5 / 26 / 100 | 26 |
| 3 | Same page with `JOIN FETCH` | 1 / 5 / 26 / 100 | 1 |
| 4 | Same page with batch fetch size 20 | 1 / 4 / 26 | 4 |
| 5 | `shortlist()` has no `save()`. Does the row update? | yes / no | yes (dirty checking) |
| 6 | `bulkShortlist([1, 2, 9999])`: what is left in the database? | 1 and 2 changed / nothing changed | nothing changed (rollback) |
| 7 | Two tabs save the same form | both succeed / second fails | second fails with 409 |
| 8 | Two `findById(1)` calls in one transaction | 2 selects / 1 select | 1 select, same object |
| 9 | Apply with an empty email | 200 form again / 500 / redirect | 200, same form with the message |
| 10 | Remove `@Transactional` from `listSlow()` | works / `LazyInitializationException` | `LazyInitializationException` |
| 11 | Jobs with no applicants in an inner-join count | shown with 0 / missing | missing (use `left join`) |
| 12 | Bulk JPQL update: does `@Version` change? | yes / no | no, unless you handle it |

---

# Part E. Interview questions by level

**Basic**
- What is ORM, and what problem does Hibernate solve compared with plain JDBC?
- JPA vs Hibernate vs Spring Data JPA: which is the rulebook, which is the implementation?
- Why `jakarta.persistence` and not `javax.persistence` in Spring Boot 3?
- `@Controller` vs `@RestController`?
- `@RequestParam` vs `@PathVariable`?

**Intermediate**
- What does `mappedBy` mean, and which side owns the relationship?
- What is N+1, and three ways to fix it (`join fetch`, `@EntityGraph`, batch size)?
- Why is `EAGER` not a fix for N+1?
- Why do we return DTOs and not entities from controllers?
- What does `@Valid` do, and where do the errors go?

**Advanced**
- What are the four entity states, and why does dirty checking work only on persistent entities?
- What is the first-level cache, and how is it different from the second-level cache?
- How does `@Version` detect a lost update?
- Why does a bulk JPQL update skip optimistic locking?
- Filter vs `HandlerInterceptor` vs `@ControllerAdvice`: when do you use each?
- Why can `IDENTITY` ids prevent JDBC insert batching?

---

# Part F. Where each example fits (Git checkpoints)

| Tag | Examples |
| --- | --- |
| `step-1-entities` | H1 |
| `step-2-repositories` | H2, H3, H4 |
| `step-3-mvc-pages` | M1, M2 |
| `step-4-validation` | M3, M5 |
| `step-5-transactions` | H7, H6 |
| `step-6-performance` | H5, H8, M4, M6 |
| `final` | everything; M7 and M8 are discussion |

Cut the tags with the steps in `docs/05`, section 6, **before** the session. The tags do not exist on the remote yet.

---

# Part G. Quick checks to run once before the session

```
./mvnw verify                                   # all existing tests + your new ones
start.cmd local                                 # H2 fallback, no Supabase needed
curl -i localhost:8080/api/jobs/popular         # H4
curl -i localhost:8080/api/jobs/paying?minLpa=6 # M1
curl -i -X POST localhost:8080/api/applications -H "Content-Type: application/json" -d '{}'   # M5
```
