# Task 7: How to Highlight Each Code Piece and Feature in the Session

Companion to the PPSU Session 2 demo guide. For every file: **what to open, what to say, what to run, what to point at**.
Running order with commands: [06-live-session-demo-script.md](06-live-session-demo-script.md); slides: [07-ppt-slide-deck-content.md](07-ppt-slide-deck-content.md); animated flow: [flow-explorer.html](flow-explorer.html).
Rule: live-code the decisions (annotations, relationship, fetch type, transaction boundary, the query fix); paste the mechanical code (getters, setters, constructors).

## 1. Order of the walkthrough (matches the Git steps)

| Step / tag | Minutes | Open | Highlight |
| --- | --- | --- | --- |
| Demos 1 to 3 | 12 | `demos/` | `java Demo1Annotations.java`, `Demo2Proxy`, `Demo3Modern` |
| `step-1-entities` | 12 | `db/migration/V1__init.sql`, entities | mapping + Flyway + validate |
| `step-2-repositories` | 8 | repositories, `NPlusOneDemo` | derived queries, JOIN FETCH |
| Demo 4 | 8 | `ApplicationService`, console | 26 -> 1 -> 4 |
| Demo 5 | 3 | `shortlist()` | dirty checking |
| Demo 6 | 3 | `DispatcherServlet.doDispatch` | request lifecycle |
| `step-3-mvc-pages` | 15 | `JobController`, `list.html`, `JobApiController` | HTML vs JSON |
| `step-4-validation` | 10 | `ApplyForm`, `GlobalExceptionHandler` | 400 / 404 / 409 |
| L5 / L6 (optional) | 6 | `bulkShortlist`, `changeStatus`, `JobRepository` | rollback, locking, performance |
| Demo 7 | 5 | browser + Supabase | end to end |

(The repository is delivered as one finished working tree; to cut the tags, commit in this order, see section 6.)

## 2. File by file

### Demos (`demos/`)
* **Demo1Annotations**: say "Hibernate is just this loop, with 300 annotations." Run; show `select full_name, email_id from student`; point out `notMapped` is ignored.
* **Demo2Proxy**: run; point at BEGIN / COMMIT around `register`. Say "`@Transactional` and Hibernate lazy proxies are this trick".
* **Demo3Modern**: record, stream grouping, switch expression, 10,000 virtual threads in about a second. "You will read all four in the repo today."

### Database (`db/migration`)
* `V1__init.sql`: point at FKs, `unique`, `check`, the `uq_application` constraint ("the real guard when two requests race") and `version`.
* `V2__seed_data.sql`: "fixed on purpose: 5, 12, 8, 20, so the N+1 number is always 26".
* `V3__perf_index.sql`: optional; explain `explain analyze`, be honest that 12 rows will use a sequential scan.
* Show Supabase Table Editor, schema `app`, plus `flyway_schema_history`.

### Entities
* `Company`: `@OneToMany(mappedBy)` "inverse side, the FK lives in job_posting".
* `JobPosting`: `@ManyToOne(fetch = LAZY)` "owning side, LAZY on purpose"; `isOpenOn` "business rule lives with the data".
* `Student`: unique email mirrors the DB constraint.
* `Application`: association entity with its own data; `@Version`.
* Start the app: validate passes only if mapping == schema. **Failure story:** the H2 schema-case mismatch we hit while building ("missing table [application]"), fixed by `DATABASE_TO_LOWER=TRUE`; shows why validate is valuable.

### Repositories
* `existsByStudentIdAndJobId`: "no implementation, Spring builds it from the name".
* `findAllWithDetails`: the N+1 fix, three `join fetch`.
* `findOpenWithCompany`, `findOpenByCity`: the same idea, plus the student challenge.
* `findAllBy` + `@EntityGraph`, `summaries` projection, `CompanyRepository.findAllWithJobCount`: the performance toolbox; "a count per row would create a new N+1".

### Services
* `ApplicationService.listSlow / listFast`: ask the class to predict the query count (poll), then run `NPlusOneDemo` (`--demo.n-plus-one=true`) and read the **Session Metrics** line.
* Bonus: delete `@Transactional` from `listSlow` -> `LazyInitializationException` (links back to Demo 2).
* `apply`: the four guards in order (exists, open, student, duplicate). Say "friendly message first, constraint last".
* `shortlist`: "which line saved it? none".
* `bulkShortlist`: run with bad id, show no UPDATE committed.
* `changeStatus`: two tabs, second gets 409.
* `Company/Student/Job` services: CRUD plus delete guards; "business rules in the service, never in the controller".

### Web layer
* `JobController`: `@GetMapping`, `@PathVariable`, `@RequestParam`, `Model`, returns a view name.
* `jobs/list.html`: `th:each`, `th:href="@{...}"`, `th:text`.
* `JobApiController`: same service, returns JSON; open `/api/jobs` next to `/jobs`.
* `ApplicationController`: `@Valid`, `BindingResult`, `RedirectAttributes`, `redirect:` (Post-Redirect-Get). Demo: submit empty form, apply twice, open `/jobs/9999`.
* `GlobalExceptionHandler` / `ApiExceptionHandler`: "the user never sees a stack trace"; HTML vs JSON from the same exception.
* Forms are records separate from entities: "never bind or expose entities".

### Configuration
* `application.properties`: `ddl-auto=validate`, `open-in-view=false`, SQL logging, statistics; datasource built from `supabase.*` properties imported from `config/supabase.properties`. Session day: `start.cmd` / `./start.sh` (health check included), `start.cmd local` as the fallback.
* `application-local.properties` / `application-batch.properties`: profiles as switches.
* `config/supabase.properties.example` and `.gitignore`: secrets hygiene; reset the password after the session.

## 3. Moments that land with the audience

1. **Predict the number** (poll) before running N+1, then count the 26 lines aloud.
2. **Same code, local database** when the network blocks Supabase.
3. **No `save()`** update.
4. **Two browser tabs** and the 409 page.
5. **Stack trace never shown**: `/jobs/9999`.
6. **Honesty slide**: what real projects add (Spring Security, caching, Docker, messaging).

## 4. If something breaks (90-second rule)

| Symptom | Move |
| --- | --- |
| Supabase unreachable | `-Dspring-boot.run.profiles=local`, say "same code, local database" |
| Compile error on stage | `git checkout step-N -- <path>` |
| Query count not 26 | check `open-in-view=false`, re-create the schema |
| Port 8080 busy | `--server.port=8081` |

## 5. Student challenge (7 minutes)

"Filter jobs by city, no extra queries." Already implemented as the reference solution: `JobRepository.findOpenByCity`, `JobService.listOpenJobs(city)`, `JobController.list`, form in `jobs/list.html`. For the session, remove those four pieces (or start from the tag before) and keep this as the answer; check success by counting one `select` per request. Stretch: pagination on `/jobs`, or a SHORTLISTED filter on applications.

## 6. Cutting the Git tags (optional)

Commit in this order and tag each: `step-0-skeleton` (pom, wrapper, `.gitignore`, `demos/`), `step-1-entities` (migrations, entities, enums, properties), `step-2-repositories` (repositories, `NPlusOneDemo`, `ApplicationService.listSlow/Fast`), `step-3-mvc-pages` (services, controllers, templates, JSON), `step-4-validation` (forms, handlers, tests), `step-5-transactions` (`bulkShortlist`, `changeStatus`), `step-6-performance` (paging, projection, batch profile, V3), `final` (README, CI, docs).

## 7. Interview questions to leave on the last slide

`get()` vs `load()`; what is N+1; `merge()` vs `persist()`; why `mappedBy`; first-level vs second-level cache; why `open-in-view=false`; difference between `@Controller` and `@RestController`; where does `@Transactional` belong.
