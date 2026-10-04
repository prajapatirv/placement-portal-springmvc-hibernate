# Speaker script, Part 1: Hibernate and Spring MVC (slide by slide)

For the deck `PPSU_Part1_Hibernate_SpringMVC_v2.pptx` (42 visible slides, 5 hidden backups). The same text is in the speaker notes of each slide; this file is the one you can print or read on a phone.

**How to use it**
- **SAY** is spoken text in simple words. Say it your own way. Do not read it.
- **DO** is something you click, type or open.
- **ASK** is a question to the room.
- **OPTIONAL** is an extra example from `docs/10-examples-basic-to-advanced.md` that you can add if the room is fast.
- Minutes are for **planning only**. No clock times appear on any slide. Announce breaks in the room once the university confirms them.

---

## 1. Context (read this once, the night before)

**Audience.** Sem-5 students of B.Tech (CE, CS, IT, ITE) and Diploma (CE, IT) at P P Savani University. They have studied Core Java, databases and web basics. Most have not built a database-backed web application. Some have used AI tools.

**What they should leave with** (say this in the first minutes and again at the end):
1. Why a framework like Hibernate exists, and how to read the SQL it writes.
2. Why Spring MVC exists, and how one request travels from the browser to the database and back.
3. One real application (the Placement Portal) they can clone, run, and extend.
4. A realistic picture of industry work, and how to prepare for it.

**The single story.** One click in the browser travels: browser, DispatcherServlet, controller, service, repository, Hibernate, database, and back. The rule for the day: **every click in the browser must be answered by a line in the console.**

**The three layers** (colour code on every slide): Data (teal, Hibernate), Web (blue, Spring MVC), Intelligence (orange, Gen AI, covered in Part 2).

**The hook.** A page that shows a simple list but asks the database 101 times. We open it on slide 11 and close it on slide 22 (our own app: 26 queries, fixed to 1).

**Poster note.** The invitation graphic shows Multithreading, JDBC, Java Web Services, Spring Boot and Microservices, and Advanced Java Features. Today's focus is Hibernate and Spring MVC, built on the JDBC and web basics they already know. Say this in one sentence at the start so nobody feels the topic changed.

**Tone.** Warm, simple English. Use Gujarati or Hindi for a hard idea if the room prefers. Nobody is behind; we start from where the room is.

---

## 2. Before you start (the day before and one hour before)

| When | Do |
| --- | --- |
| Day before | Run `./mvnw verify` once. Cut the Git tags (`docs/05`, section 6). Add the optional examples from `docs/10` on a scratch branch and run them. Print this script. |
| Day before | Test the projector cable and adapter. Download the repo to a USB stick as a backup. |
| One hour before | Reset data (`database/03_reset.sql`, then `01_create_schema.sql`) or use the `local` profile. Start with `start.cmd` (Windows) or `./start.sh`. Fallback: `start.cmd local`. |
| One hour before | Arrange windows: browser at `localhost:8080`, IDE with a big-font console, Supabase Table Editor (schema `app`), `docs/flow-explorer.html` in a second tab. Clear the console. |
| Check | Slide show mode: step through slides 4, 5, 11, 23 and 29 once to see the animations. Hidden slides B1 to B4 and B8 stay hidden. |
| Never | Put the Supabase password on a slide, in a screenshot or in Git. |

---

## 3. Time plan (minutes, flexible)

| Block | Slides | Minutes |
| --- | --- | --- |
| Opening | 1 to 5 | 7 |
| Industry view | 6 to 10 | 13 |
| The hook (101 queries) | 11 | 3 |
| Hibernate (Data) | 12 to 26 | about 47 |
| Spring MVC (Web) | 27 to 36 | about 32 |
| Placement Portal and recap | 37 to 42 | about 24 + 7 challenge |
| **Total** | | **about 133 (2 h 15 min)** |

If you are running late, cut in this order: slides 7, 9 (say them in one sentence each), 25 (skip the live part), 36, and the optional examples. Never cut slides 11, 21, 22, 24, 29 and 32. Those are the ones students remember.

---

# 4. Slide-by-slide script

## Opening

### Slide 1. Title (1 min)
**SAY:** "Good morning. Thank you for having me. Today is not a slide-reading day. We follow one click from the browser to the database and back, and then we add intelligence on top. The stack on the right is our map: Data is Hibernate, Web is Spring MVC, Intelligence is Gen AI. If you feel lost at any time, look back at this map."

### Slide 2. Welcome and thank you (1 min)
**SAY:** "Thank you to the Provost, the Dean of the School of Engineering, and the faculty coordinators from the CE-IT department for inviting me. This is the official invitation. You are Sem-5 students from B.Tech and Diploma. You already know Core Java, databases and the web basics. Today we connect those pieces into one working application."
**DO:** Confirm the names and spellings before you show this slide. If the faculty prefer not to be named on screen, delete the four cards and keep the poster.
**SAY (poster topics):** "The poster mentions JDBC, web services, Spring Boot. Today we focus on Hibernate and Spring MVC. These sit directly on top of the JDBC and web basics you know."

### Slide 3. About me (2 min)
**SAY:** "Three facts. Twelve years of building Java and Python systems in production. Five employers across five industries, so I have seen five different engineering cultures. And more than four years leading teams, so I know what managers look for when they hire."
**SAY (the path):** "I started in 2014 as a Java developer. My first projects were Spring MVC and Hibernate web apps: a theatre-ticketing system, a hotel-booking system and a timesheet tool. Then Grails products and ERP modules. In 2019 I moved to a cloud ERP used by more than thirty companies. Since 2021 I have been at Cybage on Spring Boot services and AWS data pipelines. Over the years my role moved from writing code to design, reviews, mentoring and talking to clients."
**SAY (the message):** "The tools changed a lot. The way of building logic did not. I learned the basics first and went deeper every year. That is why I never skipped the basics, and why we start with basics today."
**DO:** Do not name clients. Say "an event-management platform" and "data pipelines on AWS".

### Slide 4. Today's route (1 min)
**DO:** Let the route build itself. Do not click.
**SAY:** "Seven stops. First the industry view, so you know why the next hours matter for your career. Then Hibernate, the data layer. Then Spring MVC, the web layer. Then we put both together in one application, the Placement Portal. Then Gen AI on top, a live demo on a real product, and your questions at the end. I will announce the breaks in the room."

### Slide 5. Three layers (2 min)
**DO:** The slide builds from the bottom: Data, then Web, then Intelligence.
**SAY:** "Order a pizza on your phone. The menu you see is the web layer. The app remembering your last order and address is the data layer. When it says 'order your usual again?', that is the intelligence layer. One tap touches all three. We build them in this order today."
**OPTIONAL (canteen version):** "The storeroom is the data. The counter is the web. A very smart new intern is the intelligence."

## Industry view

### Slide 6. College vs company (2 min)
**SAY:** "College gives you the foundation. Industry adds scale, time and people. Your lab program runs once, for one teacher. A real placement portal serves thousands of students on result day, and someone must keep it running for years. Everything you learn in DBMS, OS and networks is used every day: a slow query is a DBMS problem, a stuck thread is an OS problem, a timeout is a network problem."
**ASK:** "Which subject do you think you will never use?" Take two answers. Show it is used.

### Slide 7. Service or product (2 min)
**SAY:** "Neither is better. They teach different things in your first three years. In a service company like TCS, Infosys or Cybage you build software for clients: many domains, many tools, many teams. In a product company like Zoho, Freshworks or Atlassian you go deep on one product for years. For your first job, choose the place where you will learn the most, not the logo. Many companies do both."
**DO:** Say "usually" when you talk about hiring. Do not make claims about any company's process.

### Slide 8. Why your degree counts (3 min)
**SAY (left):** "Why do companies come to universities? Your degree shows you can finish something hard for years. It shows you have the fundamentals: DBMS, OS, networks, data structures. Campus hiring gives a trainable pool, and the Placement Portal we build today models exactly that: companies post jobs, students apply. Companies can teach tools in weeks. Fundamentals take years."
**SAY (my path):** "My own path was not a straight line. BCA, an A-Level diploma, then M.Sc. IT in 2014, and my first Java job the same year. The Oracle Java certificate came in 2016, two years into the job. It supported my skills; it did not replace them."
**SAY (right):** "Five habits to be industry-ready. Code a little every day. Use SQL and Git every day. Put one real project on GitHub with a README. Take one beginner certification. Practise explaining your project in two minutes."
**SAY (resources):** "Some exams are paid, many learning badges are free. The full list is in `docs/09-resources.md` in our repo. No QR codes; clone the repo and you have it."

### Slide 9. Gen AI: good or bad? (3 min)
**SAY:** "Gen AI is a power tool. In trained hands it builds faster. In untrained hands it cuts fingers. At work we use AI assistants for code suggestions and reviews. A human still owns every decision and every line that ships. Four rules: understand before you accept; verify with tests; keep your fundamentals sharp, because interviews test your thinking, not your tool; and never paste private data into a public chatbot."
**SAY (hurdles):** "Every failed interview, bug or exam is feedback. Read the error, form a guess, test it. That is the same loop an engineer uses."

### Slide 10. Improve yourself first (3 min)
**SAY:** "Salary is the result, not the plan. Four habits, each with a meaning and an example."
**SAY (1):** "Learning over logo: a famous name with no mentor teaches you little. A team that reviews your code teaches you a lot."
**SAY (2):** "Ideas outlive versions. JDBC, Hibernate, Spring Data JPA are three generations of one idea: objects in, rows out. I started on Hibernate 3. Today's demo runs Hibernate 6. The idea is the same."
**SAY (3):** "Be reliable and visible. On Monday say 'this will slip two days'. Not on Friday 'it is not done'."
**SAY (4):** "Treat setbacks as bugs. A failure is information, not a verdict. Read the log, guess one cause, test it, repeat. Today you will see that loop with 26 queries. The same loop works after a failed interview: what question stopped me, what did I miss, study it for a week, try again."
**SAY (resources, verbal only):** "Free learning links are in `docs/09-resources.md` in the repo."

## The hook

### Slide 11. One page. 101 database queries. (3 min)
**DO:** This slide builds on your click: the 1 query, then the +100, then the sum and the banner.
**SAY:** "Imagine a job-search page. One hundred jobs on screen, each with its company name. The programmer wrote a simple loop. The page works and looks right. But behind it the database was asked 101 times: one query for the 100 jobs, then one more per job to get its company. One plus one hundred."
**ASK:** "Is the database slow?" **SAY:** "No. The code asks too often. On your laptop with five rows nobody notices. On result day with ten thousand users it becomes a crisis. Our own app has a smaller version: 20 rows, 26 queries. We fix it today. Hibernate decides how often we ask. Spring MVC decides what the user sees."

## Hibernate (Data)

### Slide 12. Why Hibernate? (3 min)
**SAY:** "To save one student with plain JDBC you open a connection, write SQL, set each parameter, run it, handle the result and close everything in `finally`. About sixty lines for one insert, then the same plumbing for update, delete and every table. With Hibernate and Spring Data it is one line. The SQL is still printed in the log, so nothing is hidden. Booking systems, ERP, banking and hospital portals all work this way. I used Hibernate in ticketing, hotel-booking and timesheet apps."

### Slide 13. Objects vs tables (2 min)
**SAY:** "Java thinks in objects. A student has a list of applications. The database thinks in tables. The application table has a `student_id` column, a foreign key. Hibernate is the translator between two languages. The sixty lines of JDBC are how we translate by hand. Pick two of the five mismatches to explain: inheritance, identity, associations, types, navigation."

### Slide 14. JPA is the rulebook (2 min)
**SAY:** "JPA is a specification, the rulebook. Hibernate is the player that follows it. Spring Data JPA sits on top and writes the boring repository methods. An interview trap: new projects use `jakarta.persistence`. The old `javax.persistence` no longer works with Spring Boot 3."
**DO:** Point at the "IN THE APP" strip: `pom.xml`, Java 21, Boot 3.5, Hibernate 6.6.

### Slide 15. A class becomes a table (4 min)
**DO:** Type the annotations on `Company` live. Do not paste. Then open `db/migration/V1__init.sql`.
**SAY:** "Annotations are sticky notes on the class. `@Entity`: this is a table. `@Id`: this is the primary key. `@Column` adds the rules. Flyway builds the database from SQL files in Git. Hibernate only checks: `ddl-auto=validate`. If the entity and the table disagree, the app refuses to start. Mapping and schema cannot drift apart."
**OPTIONAL:** Example H1 in `docs/10` (rename the `branch` column and watch the app refuse to start).

### Slide 16. CRUD without writing SQL (4 min)
**DO:** Open `/students`. Add, edit, delete. After each click point at `insert`, `update`, `delete` in the console.
**SAY:** "Look at this repository. It is an interface with a few methods and no implementation. Spring builds the code at startup. `save`, `findById`, `findAll`, `delete` come free. `findByEmail` is built from the method name."
**DO:** Try deleting a student who has applications. It is blocked by a rule in the service, not in the controller.
**OPTIONAL:** Example H2 (derived query with the CE students above CGPA 8).

### Slide 17. Relationships (3 min)
**SAY:** "A food-delivery app has the same shape. One restaurant, many dishes. Many customers, many dishes. The order is its own thing and carries a status and a time. Here: a company has many job postings. Students and postings are many-to-many, but not directly. We make `Application` its own entity because the link carries data: status, date, version."
**DO:** Show the Supabase Table Editor, schema `app`: 5 companies, 12 jobs, 8 students, 20 applications.

### Slide 18. mappedBy (3 min)
**SAY:** "The foreign key column `company_id` lives in the `job_posting` table. So `JobPosting` is the owning side. `Company` just says `mappedBy = "company"`: I do not own this, look at the field called company over there. The side with the foreign key owns the relationship. This is the most common Hibernate interview question. Notice `fetch = LAZY` on purpose. There is no EAGER anywhere in this project."

### Slide 19. Three ways to ask the database (4 min)
**DO:** Open `/jobs?city=Surat`. Show one `select` with a join in the console.
**SAY:** "Three levels, from easiest to most control. One: a derived query, from the method name. Two: JPQL with `@Query`, using class and field names. Three: a projection, when you only need two columns. Native SQL exists, but we do not need it today. And never load a million rows to show ten; the manage page uses paging."
**OPTIONAL:** Example H4 (applicants per job with `left join`, so jobs with zero applicants still show).

### Slide 20. LAZY vs EAGER (2 min)
**SAY:** "LAZY is like the menu list in a food-delivery app: names now, the full dish page only when you tap. EAGER is the app downloading every photo and review at startup, whether you look or not. Default to LAZY and fetch what you need on purpose. Switching to EAGER to fix a problem creates bigger ones."

### Slide 21. How many queries for this page? (2 min)
**DO:** Show the `listSlow()` code. Do not reveal the answer.
**ASK:** "Twenty applications, one page. One, five, twenty-six or a hundred queries? Hands up for each." Count them aloud.

### Slide 22. The answer: 26. The fix: 1. (6 min)
**DO:** The chart wipes in as the answer.
**SAY:** "Twenty-six. One list, plus eight students, twelve jobs, five companies. Nothing is broken. LAZY loading did exactly what it was told, once per row. This is the same bug as the 101."
**DO (live):** Open `/applications?slow=true`. Count the select lines aloud: 26. Open `/applications`: one select with join fetch. Optional: start with profile `local,batch` and show 4.
**DO (bonus):** Remove `@Transactional` from `listSlow()` and reload. `LazyInitializationException`. Put it back.
**SAY:** "Fix order: measure first, then pick the lightest fix. Never fix N+1 by switching to EAGER."
**OPTIONAL:** Example H5 (the "My applications" page: failing test, then `@EntityGraph`, then green).

### Slide 23. Four entity states (2 min)
**DO:** The four boxes build left to right.
**SAY:** "Like an attendance register: pencil, ink, left the room, struck off. Transient: `new Student()`, Hibernate does not know it. Persistent: inside a session, Hibernate is watching. Detached: the session closed. Removed: scheduled for delete. Only persistent objects are tracked, and that is why the next slide works."
**OPTIONAL:** Example H6 (the four-state test).

### Slide 24. No save(). Will the row update? (3 min)
**ASK:** "Read the method. We load an application, change its status, and the method ends. There is no `save()`. Will the row change?" Take a vote.
**SAY:** "Yes. Inside a transaction the object is persistent. Hibernate keeps a snapshot of what it loaded. At commit it compares and writes the difference. This is dirty checking."
**DO:** On `/applications` click Shortlist. Show `update application set status=?, version=? where id=? and version=?`. **ASK:** "Which line saved it?" None.

### Slide 25. All or nothing, and stale forms (4 min)
**SAY:** "A bank transfer must debit and credit together, or not at all. Same with bulk shortlist. Ids 1 and 2 exist, id 9999 does not. The loop changes 1 and 2, then fails on 9999. The transaction rolls back, so nothing is saved."
**DO (optional):** `curl -i -X POST localhost:8080/applications/bulk-shortlist -d "ids=1&ids=2&ids=9999"` returns 404; rows 1 and 2 are unchanged.
**SAY (right):** "Two admins open the same status form in two tabs. Tab A saves, version goes 0 to 1. Tab B saves stale data and gets a 409. The `@Version` column made the second update match zero rows. Transactions protect consistency inside one request. Versions protect it between two humans."

### Slide 26. Three rules to keep (3 min)
**SAY:** "One: look at the SQL, you cannot fix what you cannot see. Two: default to LAZY and fetch what you need on purpose. Three: keep transactions short, at the service layer."
**ASK (pair exercise, 2 minutes):** "Turn to your neighbour and explain N+1 using a waiter and a tray. A waiter who carries one dish per trip makes 26 trips for what one tray could do."

## Spring MVC (Web)

### Slide 27. Why Spring MVC? (3 min)
**SAY:** "Data is stored. The web is how people reach it. Every portal page, booking site and net-banking screen does the same three things: receive a request, run the logic, return a page or data. Without a framework you write a servlet and print HTML line by line, about forty lines for one page. With Spring MVC one annotated method does it. I built ticketing and paper-filing applications with Spring MVC and Hibernate, and worked on a Spring Boot cloud ERP."
**ASK:** "Who has written a servlet?" (hands)

### Slide 28. Do not create your objects. Ask for them. (2 min)
**SAY:** "Without dependency injection the cook goes shopping: the controller creates the service, which creates the repository, which creates the data source. Everything is glued together and impossible to test. With DI the canteen manager does the shopping and hands the cook what the cook needs. In a test you hand the cook fake vegetables, a fake repository. Use constructor injection. Field injection with `@Autowired` is a trap."
**DO:** Open `JobController`. There is a constructor taking `JobService` and no `new` anywhere.

### Slide 29. The browser never talks to your controller (5 min)
**DO:** The six boxes build one step at a time. Talk along with them.
**SAY:** "The browser talks to the DispatcherServlet, the front desk. It asks HandlerMapping 'who handles `/jobs`?', calls your controller, which calls the service and repository, gets data back, and a view renders the page. Every request uses the same front desk, so security and logging live in one place."
**DO (live):** Open `/jobs?city=Surat`. One select with a join. Optional: set a breakpoint in `JobController.list` and step through. Open `docs/flow-explorer.html` and click through flow 1.
**OPTIONAL:** Example M6 (the interceptor that prints the SQL count of every request).

### Slide 30. Controllers and mappings (4 min)
**DO:** Type the list and detail methods of `JobController`. Try `/jobs`, `/jobs?city=Surat`, `/jobs/7`.
**SAY:** "`@GetMapping` picks the URL. `@RequestParam` reads the query string, like `?city=Surat`. `@PathVariable` reads a piece of the path, like the 7 in `/jobs/7`. The method returns a view name."
**OPTIONAL:** Example M1 (`/api/jobs/paying?minLpa=6`, and the 400 when the value is missing or not a number).

### Slide 31. From Java to a web page (3 min)
**SAY:** "The controller puts data in the Model under a name, here `jobs`, and returns the view name `jobs/list`. Thymeleaf finds `templates/jobs/list.html`. `th:each` loops, `th:text` prints a value, `th:href` builds a link. The result is plain HTML sent to the browser. The template only receives DTOs, never entities. That is why `open-in-view` is off: no lazy loading inside the page."
**OPTIONAL:** Example M2 (the "closing soon" badge, with the rule kept in Java).

### Slide 32. Submit an empty form. What happens? (5 min)
**SAY:** "Never trust the form. The guard at the gate checks the ID card so the database never sees bad data."
**ASK:** "What does the user see?" Take answers. **SAY:** "The same form with 'Email is required'. A page returns 200; an API would return 400."
**DO (live):** Open `/jobs`, pick a job, Apply, submit an empty email. Then submit `yash@ppsu.example`. It redirects to `/applications` with a message and one insert. Press F5: nothing is submitted twice. That is Post-Redirect-Get.
**OPTIONAL:** `curl -i -X POST localhost:8080/jobs/1/apply -d "email="` to show the server still rejects it without the browser.

### Slide 33. HTML or JSON (3 min)
**SAY:** "A menu card is for the customer. An order slip is for the kitchen. Same dish. `@Controller` returns a view name, so a person gets an HTML page. `@RestController` returns data, so a program, like a mobile app, gets JSON. Both call the same `JobService` and run the same SQL."
**DO:** Open `/jobs` next to `/api/jobs`. Then `/jobs/9999` (friendly 404 page) and `/api/jobs/9999` (JSON 404).
**OPTIONAL:** Example M4 (`POST /api/applications` returning 201 and a Location header).

### Slide 34. One handler. Every error. (3 min)
**SAY:** "Without a global handler an unexpected error shows the user a stack trace. That is the kitchen fire in the dining room. `@ControllerAdvice` catches exceptions from every controller and turns them into readable responses: 404 not found, 409 conflict, 400 invalid form. HTML pages for browsers, JSON for APIs."
**DO:** Show `/jobs/9999`. Then apply twice with the same email: 409 'already applied'.
**OPTIONAL:** Example M5 (field-level errors for a JSON body).

### Slide 35. Controller, service, repository (3 min)
**SAY:** "Four layers, one job each. The controller is the counter: it takes the request. The service is the cook: rules and transactions. The repository is the storeroom keeper. The database is the store. Serve a plated dish, not the kitchen container: return a DTO like `JobView`, never a raw entity. Our code is organised by feature, so everything about jobs is in one folder."

### Slide 36. What real projects add (2 min)
**SAY:** "Today we build the skeleton. At a company you will see this skeleton plus extras. Already in our app: 38 automated tests, profiles, Actuator health, secrets kept out of Git. Honest next steps: Spring Security, Docker, messaging, CI/CD. A good first one after today is Spring Security."

## The Placement Portal

### Slide 37. One app carries every topic (2 min)
**SAY:** "Everything we discussed lives in one application. This table is your map: the topic, the file where it lives, and the page where you can see it. During the lab pick any row, open the file, find the line, open the page, then find the SQL in the console."

### Slide 38. Project structure (3 min)
**DO:** Open the project in the IDE while you explain.
**SAY:** "Package by feature: everything about jobs is in the `job` folder. If you change how jobs work, you touch one folder. Packaging by layer would scatter one feature across three folders. `db/migration` holds the Flyway SQL: V1 schema, V2 seed, V3 index. `templates` holds the Thymeleaf pages. `docs` holds the design notes and the flow explorer."

### Slide 39. Run it end to end (8 min)
**DO:** Browser, then console, then Supabase, in this order: `/jobs`; apply with an empty email then a valid one; `/applications?slow=true`, then `/applications`; then the Supabase Table Editor.
**SAY:** "Say the click, point at the SQL." Repeat the golden rule: every click answered by a line in the console.
**DO (fallback):** If Supabase is unreachable, run `start.cmd local` and say "same code, local database".

### Slide 40. Supabase gotchas (2 min)
**SAY:** "Four traps that cost people an hour. The direct connection needs IPv6, so use the Session pooler, not the Transaction pooler. Always `sslmode=require`. Our tables live in schema `app`, not `public`. And never put a password in Git, a file, a screenshot or a commit message. `config/supabase.properties` is git-ignored. Reset the password after a public session."

### Slide 41. Stuck? Jump to a checkpoint (3 min + 7 min challenge)
**SAY:** "The repo has checkpoints. If your code breaks, check out the tag for the step you are on and continue from there: `git checkout -b my-work step-2-repositories`."
**SAY (challenge):** "Seven minutes. Filter jobs by city, with exactly one query. Hint: look at how the other `join fetch` queries are written. You succeed when the console shows one select per request."
**DO:** The tags and a starting point without `findOpenByCity` must exist in the repo before the session.
**OPTIONAL (for fast students):** pick from the challenge board in `docs/10`, Part C.

### Slide 42. Recap and questions (5 min)
**SAY:** "Two sentences to remember. Hibernate: look at the SQL, default to LAZY, keep transactions short. Spring MVC: request in, response out, one front door. If you can explain these questions, you can answer them in an interview: what is N+1 and how do you fix it; why `mappedBy`; `@Controller` vs `@RestController`; where does `@Transactional` belong."
**DO:** Ask them to clone the repo and check out `step-2-repositories` so they are ready for the lab. Then open for questions.
**SAY (hand-off to Part 2):** "So far we built the data layer and the web layer. Next we add the third layer, intelligence."

---

## 5. If something breaks (90-second rule)

| Symptom | Move |
| --- | --- |
| Supabase unreachable | `start.cmd local`, say "same code, local database" |
| Compile error on stage | `git checkout step-N -- <path>` |
| Query count is not 26 | check `open-in-view=false`, re-create the schema |
| Port 8080 busy | `--server.port=8081` |
| Projector or adapter fails | go to the board and draw the request flow with the class (backup B8) |
| Live demo fails | show the hidden backup slides B1 to B4 and say "here is what it prints" |
| An optional example does not compile | skip it, say "that one is in the repo docs for you to try" |

## 6. Questions you may get

| Question | Short answer |
| --- | --- |
| Is Hibernate still used? | Yes. JPA with Hibernate is the standard in most Java enterprise projects. |
| Do I need to learn JDBC first? | Know the idea (connection, statement, result). You rarely write it by hand. |
| Why not just use SQL everywhere? | You can. ORM saves plumbing and keeps mapping in one place; you still must understand the SQL. |
| Is EAGER ever right? | Sometimes, for small, always-needed data. As a default, no. |
| Will AI replace developers? | AI changes how we write code. Someone must still design, review, test and own the result. |
| Which should I choose, service or product? | Where you will learn the most in your first three years. |

## 7. After the session
- Share the repo link and `docs/09-resources.md` in the class group.
- Ask the faculty to collect questions you could not answer, and answer them in the group.
- Reset the Supabase password.
