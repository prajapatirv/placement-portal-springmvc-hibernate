# Session examples: the runnable appendix

Sixteen examples, basic to advanced, built into the app. Start the app and open **`/examples`**.

```
start.cmd local        # or: start.cmd   (Supabase)
http://localhost:8080/examples
```

Each example has its own page, `/examples/h1` ... `/examples/m8`, with:

| Block | What it is for |
| --- | --- |
| Predict (Reveal button) | ask the room first, then reveal |
| Code snippets | the exact code, big font, ready to read aloud (A+ / A- buttons zoom for the projector) |
| Run buttons | call the real endpoint from the page and show the HTTP status |
| Request log | method, URL, status, **SQL count**, milliseconds: the same lines the console prints (example M6) |
| Break it / Say / Watch | the talk track |

**Move one example at a time:** the left and right arrow keys, or the Prev / Next buttons. The index (`/examples`) is the appendix page: one row per example, with the slide numbers it belongs to.

## Moving between a slide and an example

The deck stays the story; the example page is the proof. One simple rhythm per topic:

1. Show the slide (concept, 1 to 2 minutes).
2. Alt-Tab to the browser tab already open on `/examples/<id>`.
3. Predict, Reveal, read the snippet, press **Run**, point at the SQL column of the request log.
4. Press the right arrow only when the topic changes; Alt-Tab back to the deck.

Keep two browser windows: the app pages (`/jobs`, `/applications`) and `/examples/<id>`. Put the IDE console on a third screen or leave it for the backup.

| Slide(s) | Example | Slide(s) | Example |
| --- | --- | --- | --- |
| 15 A class becomes a table | H1 | 30 Controllers and mappings | M1 |
| 16 CRUD | H2 | 31 Java to a web page | M2 |
| 17, 18 Relationships, mappedBy | H3 | 32 Empty form | M3 |
| 19 Three ways to ask | H4 | 33 HTML or JSON | M4 |
| 20, 21, 22 LAZY, N+1 | H5 | 34 One handler, every error | M5 |
| 23, 24 Lifecycle, dirty checking | H6 | 35 Layers, DTO | M7 |
| 25 All or nothing, stale forms | H7 | 36 What real projects add | M8 |
| none yet | H8 (optional), M6 | | |

H8 and M6 have no slide in the v3 deck. Add two appendix slides ("Bulk update: one statement, but no version" and "An interceptor that counts SQL") that only show the title and the URL `/examples/h8` or `/examples/m6`, or just open the page from the index.

## What changed in the code

| Example | Added |
| --- | --- |
| H2 | `StudentRepository.findByBranchIgnoreCaseAndCgpaGreaterThanEqualOrderByCgpaDesc`; `/students?branch=CE&minCgpa=8` |
| H3, H5 | `ApplicationRepository.findByStudentEmailIgnoreCase` (naive) and `findWithGraphByStudentEmailIgnoreCase` (entity graph); `/applications/mine?email=...&slow=true` |
| H4 | `JobRepository.applicantsPerJob`, `JobApplicantCount`; `/api/jobs/popular` |
| H6, H8, H1 | `examples/ExampleLab`: each demo runs in a transaction that is rolled back, so it can be repeated |
| H8 | `ApplicationRepository.updateStatusIn`, `ApplicationService.bulkShortlistFast` |
| M1 | `/api/jobs/paying?minLpa=6` |
| M2 | `JobView.closingSoon()` and the badge in `jobs/list.html` |
| M4 | `ApplicationApiController` (201 + Location, plus a DELETE so the demo can be undone) |
| M5 | `MethodArgumentNotValidException` handler in `ApiExceptionHandler` |
| M6 | `QueryCountInterceptor`, `WebConfig`, `RequestLog` |
| M7 | `/examples/run/m7/raw-entity`: wrong on purpose, returns a 500 |

`ExamplesTest` (13 tests) runs every Run button that does not change data and checks the status code, and asserts the numbers we say aloud (8 statements naive, 1 with the entity graph, 26 vs 1, 6 vs 1, and so on). `./mvnw verify` now runs 51 tests.

## Before the session: things to know

- **Data-changing buttons:** only M4 changes data. After "New application" an **Undo** button appears; press it. On the `local` profile a restart resets everything.
- **H4 zero applicants:** in the seed data every job has at least one applicant, so the PREDICT answer cannot be seen until you add a posting on `/jobs/new` (nobody applied to it) and run the button again. Delete it afterwards.
- **M2 badge:** no posting closes within 3 days. Edit a last date to tomorrow on `/jobs/{id}/edit`, then reload `/jobs`.
- **M3 happy path** and **H7 two tabs** are manual on purpose (a redirect and two humans).
- **M6 counter is global:** with several people using the app, the numbers mix. Demo it on your own machine.
- **H1 break-it, H5 break-it:** these need a code edit and a restart; do them in the IDE and revert.
- The request log shows what the app did, not the console text. Keep the console open for the SQL text itself.
