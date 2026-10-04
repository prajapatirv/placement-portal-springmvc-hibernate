# 12 · Interactive architecture walkthrough

One self-contained page that explains the whole Campus Placement Portal stack, in three parts, with animated diagrams. It needs **no login and no build step**, and it works offline (it only tries to load optional Google Fonts and falls back to system fonts) — it is a single static HTML file served by the app itself.

- **Open it in the running app:** `http://localhost:8080/demo` (page inside the app layout) or `http://localhost:8080/demo/walkthrough.html` (full-screen, for the projector).
- **Open it without the app:** double-click `src/main/resources/static/demo/walkthrough.html`.
- **Keys:** `Space` run/pause · `→` next step · `←` previous · click any step on the right to jump to it · click any box in a diagram to read what it does.

Visual code: **orange** = web layer (Spring MVC) · **teal** = data layer (Hibernate, PostgreSQL) · **violet** = the only AI box · **green** = code guard · **gold** = human decision · **red** = blocked.

The whole walkthrough rests on one rule: **code verifies and executes; the model only names a tool or narrates verified facts.** The AI never writes SQL and has no database connection.

---

## Part 1 — ORM with Hibernate, and where AI connects

| # | Tab | What it shows | Matching material |
|---|-----|---------------|-------------------|
| 1 | Big picture | One request down through Controller → Service → Hibernate → JDBC → PostgreSQL and back up as objects, then the small AI road beside it | Part 1 deck, architecture slides |
| 2 | Mapping | `Company`, `JobPosting`, `Application`, `Student` ↔ tables; `@OneToMany(mappedBy)`, `@ManyToOne(LAZY)`, unique pair, `@Version`, `ddl-auto=validate` | `docs/10`, tag `step-1-entities` |
| 3 | Inside Hibernate | Session / persistence context, first-level cache, dirty checking, flush, optimistic-lock failure | Demo 5 (`shortlist`) |
| 4 | Entity lifecycle | Transient → Persistent → Detached → Removed; why the AI receives plain records, never entities | `LazyInitializationException` bonus |
| 5 | Lazy loading · N+1 | Slow `findAll()` = 1+8+12+5 = **26** queries, fast `JOIN FETCH` = **1**; live counter | Demo 4, tag `step-2-repositories` |
| 6 | Where AI connects | A question travels seven layers; only layer ③ is AI. Pick *placed students*, *shortlisted*, or *delete all rejected students* (refused at layer ④). Toggle mock / live | Placement Assistant add-on |

**Say-this-live line:** "The AI is one box. Everything else is the Spring and Hibernate you learned this morning."

## Part 2 — Spring MVC internals → the Campus Placement app

| # | Tab | What it shows | Matching material |
|---|-----|---------------|-------------------|
| 1 | Architecture | `DispatcherServlet`, `HandlerMapping`, `HandlerAdapter`, controller, `ViewResolver`, Thymeleaf — for `GET /jobs` | Demo 6 (breakpoint in `doDispatch`) |
| 2 | Beans · DI | Component scan, bean creation order (DataSource → SessionFactory → repositories → services → controllers), constructor injection, singletons, `AiClient` mock/live wiring | `assistant.mode` property |
| 3 | Forms · validation · PRG | Binding, `@Valid`, `BindingResult`, redirect-after-post; valid vs empty email | tag `step-4-validation` |
| 4 | Phase 1 · Publish jobs | `GET /jobs`, `findOpenWithCompany()` | tag `step-3-mvc-pages` |
| 5 | Phase 2 · Apply | `POST /jobs/{id}/apply`, `ApplicationService.apply()`, status `APPLIED`, 302 redirect | `docs/10` |
| 6 | Phase 3 · Review (admin) | `GET /applications?slow=true` vs default: 26 queries vs 1 | N+1 payoff |
| 7 | Phase 4 · Shortlist · select | `APPLIED → SHORTLISTED → SELECTED`, no `save()`, optimistic locking | Demo 5 |
| 8 | Phase 5 · JSON · AI | `@RestController` at `/api/jobs`, same service layer, hand-off to Gen AI | `JobApiController` |

**Say-this-live line:** "You write the controller and the template. The framework does the other six jobs."

## Part 3 — Gen AI: from basics to agents, and the whole stack

| # | Tab | What it shows |
|---|-----|---------------|
| 1 | The AI ladder | Rules → ML → LLM → LLM + tools → agent → agentic pipeline, with where each lives in our project |
| 2 | Hallucination · grounding | Ungrounded answer ("14 students placed") vs facts fetched by code first; missing-facts list |
| 3 | Tool-use loop | Model asks, code validates and runs, result returns; loop capped at three rounds |
| 4 | Placement Assistant | `AiClient` interface, `MockAiClient` vs `ClaudeAiClient`, same whitelist either way |
| 5 | Human approval (AI Desk) | Draft → validator → human → send; outcomes *approved*, *rejected*, *blocked by validator* |
| 6 | The whole stack | One question through all eight layers, 1 SQL statement, 1 AI call that only named a tool |

**Say-this-live line:** "Switching from mock to live changes who picks the tool. It never changes what is allowed."

---

## Suggested use in the session

1. **Part 1, tab 5 (N+1):** run it slow, then fast. Let the counter do the talking.
2. **Part 2, tab 1:** run once, then repeat in the IntelliJ debugger with the breakpoint.
3. **Part 2, tab 3:** switch *empty email* to show that bad input never reaches SQL.
4. **Part 3, tab 6:** finish here; it ties all three parts together in one run.

## Accuracy notes — check before the session

- Class names, routes, queries and the 26 → 1 count come from the demo guide (`ApplicationService`, `JobController`, `JobApiController`, `findAllWithDetails`, `findOpenWithCompany`).
- The **Placement Assistant** pieces (`AiClient`, `MockAiClient`, `ClaudeAiClient`, `ToolRegistry`, the tool names `placed_students`, `applications_by_status`, `students_for_company`, `assistant.mode`) are taken from the add-on design. If your code uses different names, edit the matching strings in `walkthrough.html` (search for the name).
- The **AI Desk** and **Q&A** scenes describe the saaviragroup.in dashboard design, not this repo.
- Row counts in the walkthrough (12 open jobs, 20 applications, 1 `SELECTED`, 4 `SHORTLISTED`) follow the seed data in `V2__seed_data.sql`. Re-run the app and compare.

## Files

| File | Purpose |
|------|---------|
| `src/main/resources/static/demo/walkthrough.html` | The whole walkthrough, one file |
| `src/main/resources/templates/demo.html` | Thin Thymeleaf page that shows it inside the app layout (tab "Architecture Demo") |
| `docs/12-interactive-walkthrough.md` | This file |
