package com.ppsu.placement.examples;

/**
 * The "How it works inside" section of every example. The SQL is copied from the console of a real run on the
 * local profile (Hibernate 6, H2 in PostgreSQL mode), only re-flowed onto fewer lines. On Supabase the text of
 * the SQL is the same except for small dialect differences (for example fetch first ? rows only).
 */
final class ExampleInternals {
    private ExampleInternals() {}

    static Example add(Example e) {
        return switch (e.id) {
            case "h1" -> h1(e);
            case "h2" -> h2(e);
            case "h3" -> h3(e);
            case "h4" -> h4(e);
            case "h5" -> h5(e);
            case "h6" -> h6(e);
            case "h7" -> h7(e);
            case "h8" -> h8(e);
            case "m1" -> m1(e);
            case "m2" -> m2(e);
            case "m3" -> m3(e);
            case "m4" -> m4(e);
            case "m5" -> m5(e);
            case "m6" -> m6(e);
            case "m7" -> m7(e);
            case "m8" -> m8(e);
            default -> e;
        };
    }

    // ===================================== Part A. Hibernate =====================================

    private static Example h1(Example e) {
        return e
                .inside("What happens at startup, in order", "", "text", """
                        1. Flyway runs V1, V2, V3 and creates the tables in schema app.
                        2. Hibernate scans the @Entity classes and builds metadata: class -> table, field -> column, Java type -> SQL type.
                        3. ddl-auto=validate: Hibernate reads the real tables through JDBC metadata and compares.
                              table exists?  every mapped column exists?  type compatible?
                        4. Any mismatch -> SchemaManagementException, and Tomcat never starts.""")
                .inside("What validate does not check",
                        "It checks that tables and columns exist and that types fit. It does not check not-null, unique or foreign-key constraints: those are Flyway's job.",
                        "", null)
                .inside("Names you did not write",
                        "No @Column(name=...) on appliedAt, yet the SQL says applied_at. Spring Boot's naming strategy turns camelCase into snake_case. You can see it in every select in the console.",
                        "text", """
                        appliedAt   -> applied_at
                        maxPackageLpa -> max_package_lpa
                        class JobPosting + @Table(name = "job_posting") -> app.job_posting   (schema from hibernate.default_schema)""")
                .inside("IDENTITY: who creates the id?",
                        "The database does. Hibernate sends the insert with 'default' for the id and reads the generated key back, so the insert must run immediately, not at commit.",
                        "sql", """
                        insert into app.student (branch, cgpa, email, name, id)
                        values (?, ?, ?, ?, default)""")
                .inside("EnumType.STRING", "Hibernate writes status.name(). With ORDINAL it would write status.ordinal(), a position that changes when the enum is reordered.",
                        "text", "STRING   OPEN, CLOSED     stable\nORDINAL  0, 1             breaks when the enum order changes");
    }

    private static Example h2(Example e) {
        return e
                .inside("From method name to rows: the pipeline", "", "text", """
                        startup   Spring creates a proxy that implements StudentRepository (there is no class you wrote).
                        parse     PartTree splits the name: subject (find) + By + predicates (Branch, Cgpa) + OrderBy.
                        validate  every property must exist on Student. A typo (findByBrnch) stops the app at startup.
                        build     the parsed tree becomes a JPA Criteria query and is cached per method.
                        run       Hibernate turns Criteria into SQL, JDBC runs it, rows become Student objects.""")
                .inside("The name, word by word", "", "text", """
                        findBy                  select the entity
                        Branch                  where branch = ?
                        IgnoreCase              ... upper(branch) = upper(?)
                        And
                        Cgpa                    cgpa
                        GreaterThanEqual        >= ?
                        OrderByCgpaDesc         order by cgpa desc""")
                .inside("The SQL in the console (this run)", "", "sql", """
                        select s1_0.id, s1_0.branch, s1_0.cgpa, s1_0.email, s1_0.name
                        from app.student s1_0
                        where upper(s1_0.branch) = upper(?) and s1_0.cgpa >= ?
                        order by s1_0.cgpa desc""")
                .inside("save() and delete() are not magic either",
                        "save(): if the id is null Hibernate persists (insert), otherwise it merges (a select, then an update if something changed). delete(): load the row, then delete it. With @Version the delete is checked too: delete ... where id=? and version=?",
                        "sql", """
                        select ... from app.application a1_0 where a1_0.id=?
                        delete from app.application where id=? and version=?""");
    }

    private static Example h3(Example e) {
        return e
                .inside("Who owns the relationship?",
                        "Hibernate asks one question: which table has the foreign key column? Application has student_id and job_id, JobPosting has company_id. Those classes hold @ManyToOne + @JoinColumn: the owning side. The other side (Company.postings) is only a convenience view with mappedBy; changing that list alone writes nothing.",
                        "text", """
                        company 1 ----< job_posting N ----< application N >---- 1 student
                                  company_id (FK)       job_id (FK)   student_id (FK)""")
                .inside("What LAZY really is",
                        "Application.job is not a JobPosting. It is a generated subclass (a proxy) that only knows its id. Reading getId() is free. Reading getTitle() makes the proxy run select ... where id=?, and that needs an open session.",
                        "text", """
                        a.getJob()            -> proxy, 0 SQL
                        a.getJob().getId()    -> 0 SQL (the proxy already has the id)
                        a.getJob().getTitle() -> select ... from app.job_posting where id=?""")
                .inside("The 8 statements of the naive page, in order (this run: Asha, 3 applications)", "", "text", """
                        1  select a.* from application a join student s on s.id=a.student_id where upper(s.email)=upper(?)
                           (the join is only for the where; no student columns are fetched)
                        2  select ... from student where id=?        when toRow reads the student name
                        3  select ... from job_posting where id=?    application 1 -> job
                        4  select ... from company where id=?        job -> company
                        5  select ... from job_posting where id=?    application 2 -> job
                        6  select ... from company where id=?
                        7  select ... from job_posting where id=?    application 3 -> job
                        8  select ... from company where id=?
                        The student is loaded once, not three times: the second request for student #1 is answered from the first-level cache (see H6).""");
    }

    private static Example h4(Example e) {
        return e
                .inside("Derived query: how existsByTitleIgnoreCase becomes SQL", "", "text", """
                        existsBy      -> existence check, returns boolean
                        Title         -> the title property of JobPosting
                        IgnoreCase    -> compare without regard to case

                        startup:  proxy for JobRepository  ->  PartTree parses the name  ->  checks title exists
                        call:     Criteria query  ->  Hibernate SQL  ->  JDBC  ->  true or false""")
                .inside("The SQL we actually saw (not a count)",
                        "Spring Data 3 does not run count(*) for exists. It selects one id and stops at the first row, which is cheaper. Hibernate 6 writes the case-insensitive match with upper(), not lower().",
                        "sql", """
                        select jp1_0.id
                        from app.job_posting jp1_0
                        where upper(jp1_0.title) = upper(?)
                        fetch first ? rows only""")
                .inside("Same property, three different names", "", "text", """
                        existsByTitle(t)                        where title = ?
                        existsByTitleIgnoreCase(t)              where upper(title) = upper(?)
                        existsByTitleContainingIgnoreCase(t)    where upper(title) like upper(?)   (value wrapped as %t%: partial match)""")
                .inside("Worked example with our data",
                        "existsByTitleIgnoreCase(\"java backend intern\") is true because the table holds 'Java Backend Intern'. existsByTitle(\"java backend intern\") is false: exact, case-sensitive match. This is exactly how JobService.validate stops duplicate postings: try POST /jobs twice with the same title and watch this select run first.",
                        "", null)
                .inside("JPQL: you write HQL, Hibernate writes SQL", "", "text", """
                        you write      select j from JobPosting j join fetch j.company c where lower(c.city) = lower(:city)
                        Hibernate      parses it (class and field names, not table names), checks them, translates
                        console        select jp1_0.id, ..., c1_0.id, c1_0.city, ... from app.job_posting jp1_0
                                       join app.company c1_0 on c1_0.id=jp1_0.company_id
                                       where lower(c1_0.city)=lower(?) and jp1_0.status='OPEN' order by jp1_0.last_date""")
                .inside("Projection: a proxy that has two getters",
                        "JobSummary is an interface. Spring creates an object that implements it and fills it from a tuple of two columns. No entity is created, nothing enters the persistence context, nothing can be dirty-checked.",
                        "sql", """
                        select jp1_0.title, c1_0.name
                        from app.job_posting jp1_0
                        join app.company c1_0 on c1_0.id = jp1_0.company_id
                        where jp1_0.status = ?""")
                .inside("The aggregate query: why left join",
                        "An inner join drops a job with no application row. The left join keeps it, and count(a1_0.id) counts the non-null ids, so it gives 0. The count(a) in the HQL becomes count(a1_0.id).",
                        "sql", """
                        select jp1_0.title, count(a1_0.id)
                        from app.job_posting jp1_0
                        left join app.application a1_0 on a1_0.job_id = jp1_0.id
                        group by jp1_0.title
                        order by count(a1_0.id) desc, jp1_0.title""");
    }

    private static Example h5(Example e) {
        return e
                .inside("Why 26? Count them", "", "text", """
                        findAll()                          1   select ... from application
                        a.getStudent().getName()           8   one select per distinct student (8 students)
                        a.getJob().getTitle()             12   one select per distinct job (12 jobs)
                        a.getJob().getCompany().getName()  5   one select per distinct company (5 companies)
                                                          --
                                                          26
                        Rows repeat, but each distinct id is loaded once: the first-level cache answers the rest.""")
                .inside("Three fixes, three different SQL shapes", "", "text", """
                        JOIN FETCH        1 statement: one wide select with joins to student, job_posting, company
                        @EntityGraph      the same idea, declared on the method instead of in the query text
                        batch size 20     4 statements: the lazy selects stay, but become
                                          select ... from student where id in (?,?,?,...)  (up to 20 ids at a time)""")
                .inside("The entity graph SQL, honestly",
                        "My applications with the entity graph is one statement. Look closely and you will see student joined twice: once for the where clause (the derived query by student email) and once to fetch the columns. It is still one round trip, and fine at this size. A hand-written join fetch query would join it once.",
                        "sql", """
                        select a1_0.id, ..., j1_0.*, c1_0.*, ..., s2_0.*, a1_0.version
                        from app.application a1_0
                        join app.student s1_0 on s1_0.id = a1_0.student_id        -- for the where
                        join app.job_posting j1_0 on j1_0.id = a1_0.job_id
                        join app.company c1_0 on c1_0.id = j1_0.company_id
                        join app.student s2_0 on s2_0.id = a1_0.student_id        -- to fetch the columns
                        where upper(s1_0.email) = upper(?)""")
                .inside("Why LazyInitializationException?", "", "text", """
                        1. A lazy proxy can only select while its session is open.
                        2. The session lives as long as the transaction (@Transactional).
                        3. open-in-view=false stops Spring from keeping it open while the view renders.
                        4. So: touch a lazy field after the service method returned -> no session -> exception.
                        The fix is not EAGER: fetch what the page needs inside the service, as listFast() and the entity graph do.""");
    }

    private static Example h6(Example e) {
        return e
                .inside("Dirty checking in five steps", "", "text", """
                        1. findById(id)  -> select ...; Hibernate stores the entity AND a hidden copy of its field values (the snapshot).
                        2. a.setStatus(SHORTLISTED)  -> only changes the Java object. No SQL yet.
                        3. flush (before commit, or before a query that needs fresh data)
                        4. Hibernate compares every persistent entity with its snapshot field by field.
                        5. Different -> update. Same -> nothing. A detached or new object is never compared.""")
                .inside("The update that was printed", "No @DynamicUpdate, so every column is written, not just status. The where clause carries the old version (that is optimistic locking, H7), and version is incremented in the same statement.",
                        "sql", """
                        update app.application
                        set applied_at=?, job_id=?, status=?, student_id=?, version=?
                        where id=? and version=?""")
                .inside("The four states and the SQL each step caused (this run)", "", "text", """
                        new Student(...)       TRANSIENT    no SQL. Hibernate has never heard of it.
                        em.persist(s)          PERSISTENT   insert into app.student (branch, cgpa, email, name, id) values (?, ?, ?, ?, default)
                                                            (IDENTITY: it runs immediately; with a sequence it would wait for the flush)
                        em.detach(s)           DETACHED     no SQL. Later setName() is invisible to Hibernate.
                        em.find(Student, id)   PERSISTENT   select ... from app.student where id=?   (a new, different object)
                        em.remove(x); flush    REMOVED      delete from app.student where id=?""")
                .inside("First-level cache", "", "text", """
                        Where:  inside the session (EntityManager), a map keyed by entity type + id. One per transaction.
                        find(1) -> look in the map -> miss -> select -> store
                        find(1) -> look in the map -> hit  -> same Java object, 0 SQL
                        clear() -> empties the map; the next find(1) selects again (this run: 1 select, a different object)
                        You cannot switch it off. The second-level cache is a different, optional thing shared across sessions.""");
    }

    private static Example h7(Example e) {
        return e
                .inside("What @Transactional really does", "", "text", """
                        Spring wraps JobService / ApplicationService in a proxy.
                          call  -> proxy: begin transaction (get a connection, setAutoCommit(false))
                                -> your method runs
                                -> returns normally: flush + commit
                                -> RuntimeException: rollback (checked exceptions do NOT roll back by default)
                        Calling a @Transactional method from another method of the same class skips the proxy: no transaction.""")
                .inside("The rollback we saw (bulkShortlist [1, 2, 9999])",
                        "Three selects, zero updates. The changes to #1 and #2 existed only in memory (dirty), the flush never came because NotFoundException left the method, and the rollback threw the changes away.",
                        "text", """
                        select ... from app.application a1_0 where a1_0.id=?    #1  setStatus(...) in memory
                        select ... from app.application a1_0 where a1_0.id=?    #2  setStatus(...) in memory
                        select ... from app.application a1_0 where a1_0.id=?    #9999 no row -> NotFoundException
                        (no update, ROLLBACK)""")
                .inside("Optimistic locking, step by step", "", "text", """
                        Tab A and tab B both load version 0.
                        A saves:  update ... set status=?, version=1 where id=? and version=0   -> 1 row   -> ok
                        B saves:  update ... set status=?, version=1 where id=? and version=0   -> 0 rows
                                  Hibernate sees 0 rows -> ObjectOptimisticLockingFailureException -> 409
                        No row is locked while a human thinks. The check happens only at the moment of writing.""")
                .inside("Why changeStatus compares the version itself",
                        "The form is minutes old, but Hibernate only knows the version it loaded a moment ago inside this transaction. So the service compares the version that travelled in the form with the current one first, and throws the same exception.",
                        "java", """
                        if (!a.getVersion().equals(form.version()))
                            throw new ObjectOptimisticLockingFailureException(Application.class, form.id());""");
    }

    private static Example h8(Example e) {
        return e
                .inside("Two ways, side by side (this run, 3 applications)", "", "text", """
                        LOOP (bulkShortlist)               6 statements
                          3 x select ... from app.application where id=?
                          3 x update app.application set applied_at=?, job_id=?, status=?, student_id=?, version=? where id=? and version=?
                          versions 0,0,0 -> 1,1,1

                        ONE UPDATE (bulkShortlistFast)     1 statement
                          update app.application a1_0 set status=? where a1_0.id in (?, ?, ?)
                          versions 0,0,0 -> 0,0,0      <- the version did not move""")
                .inside("What the two annotation flags do", "", "text", """
                        flushAutomatically = true   before the update, flush pending changes so the update sees them
                        clearAutomatically = true   after the update, empty the first-level cache, because loaded
                                                    Application objects would still show the old status""")
                .inside("Why the version does not move",
                        "A JPQL bulk update is sent to the database as written. Hibernate does not load entities, so there are no snapshots and no dirty checking, and the version column is only maintained by that machinery. Add 'versioned' to the HQL or set version = version + 1 yourself, and test it before relying on it.",
                        "", null);
    }

    // ===================================== Part B. Spring MVC =====================================

    private static Example m1(Example e) {
        return e
                .inside("What happens between the browser and your method", "", "text", """
                        GET /api/jobs/paying?minLpa=6
                        1  Tomcat gives the request to DispatcherServlet (the front desk).
                        2  HandlerMapping: which method? It compares URL + HTTP method with every @GetMapping.
                           /api/jobs/paying matches the literal mapping before the pattern /api/jobs/{id}.
                        3  HandlerAdapter calls the method after resolving each parameter.
                        4  @RequestParam BigDecimal minLpa: take the text "6" from the query string,
                           ConversionService turns String -> BigDecimal.
                        5  Your method runs. The return value (List<JobView>) goes to Jackson -> JSON.""")
                .inside("The three outcomes we ran", "", "text", """
                        ?minLpa=6      200  converted, controller runs, 1 SQL
                        (missing)      400  MissingServletRequestParameterException   before your code, 0 SQL
                        ?minLpa=abc    400  MethodArgumentTypeMismatchException        conversion failed, 0 SQL
                        Spring maps both to 400 with DefaultHandlerExceptionResolver. You wrote no error handling.""")
                .inside("The SQL (derived query + entity graph)", "", "sql", """
                        select jp1_0.id, jp1_0.company_id, c1_0.id, c1_0.city, c1_0.name, c1_0.website,
                               jp1_0.last_date, jp1_0.max_package_lpa, jp1_0.min_package_lpa, jp1_0.status, jp1_0.title
                        from app.job_posting jp1_0
                        join app.company c1_0 on c1_0.id = jp1_0.company_id
                        where jp1_0.min_package_lpa >= ?
                        order by jp1_0.min_package_lpa desc""")
                .inside("@RequestParam versus @PathVariable", "", "text", """
                        /jobs?city=Surat   @RequestParam   after the ?, optional filters
                        /jobs/7            @PathVariable   part of the path, identifies one thing""");
    }

    private static Example m2(Example e) {
        return e
                .inside("From return value to HTML", "", "text", """
                        1  list() puts "jobs" into the Model (a map that lives for this request) and returns "jobs/list".
                        2  DispatcherServlet gives the name to the ViewResolver. Thymeleaf's resolver adds the prefix and suffix:
                           classpath:/templates/jobs/list.html
                        3  Thymeleaf reads the file, evaluates each th:* attribute with SpEL against the Model, writes HTML.
                        4  th:each="j : ${jobs}" repeats the row; ${j.closingSoon()} calls the method on the JobView record.""")
                .inside("Why the page gets records, not entities",
                        "By the time Thymeleaf runs, the service method has returned and the transaction is over. An entity with a lazy field would try to select with no session (LazyInitializationException, see H5). JobView is plain data: safe to read at any time.",
                        "", null)
                .inside("Why the rule is a Java method",
                        "A condition like th:if=\"${j.lastDate.isAfter(...)}\" cannot be unit tested and gets copied between pages. closingSoon() is tested in ExamplesTest in one line.",
                        "", null);
    }

    private static Example m3(Example e) {
        return e
                .inside("What happens on POST /jobs/1/apply", "", "text", """
                        1  DispatcherServlet picks apply() by URL + POST.
                        2  @ModelAttribute("form"): Spring reads the form fields and builds ApplyForm(email).
                        3  @Valid: Hibernate Validator checks @NotBlank and @Email on the record.
                        4  Violations are not thrown: they are put into the BindingResult that comes right after the form parameter.
                           (Without a BindingResult parameter Spring throws, and the user gets a 400.)
                        5  Your code decides: errors -> return "apply" (the same page, status 200).
                           ok -> service.apply(...) and return "redirect:/applications" (status 302).""")
                .inside("What the console showed for the empty email",
                        "No insert and no service call, but one select: the controller reloads the job so the form page can show the job title again. Validation itself costs 0 SQL.",
                        "sql", "select jp1_0.id, ..., c1_0.* from app.job_posting jp1_0 join app.company c1_0 ... where jp1_0.id=?")
                .inside("Post-Redirect-Get", "", "text", """
                        POST /jobs/1/apply       -> 302 Location: /applications       (the browser's last request is now a GET)
                        GET  /applications       -> 200 list page
                        F5 repeats the GET, so nothing is submitted twice.
                        The green 'Application submitted' message is a flash attribute: kept in the session for exactly one request.""")
                .inside("The apply rules and the SQL they cause (from the run of the happy path in M4)", "", "text", """
                        1 select job_posting where id=?                        does the job exist?   (not found -> 404)
                        2 select student where email=?                         does the student exist?
                        3 select a.id ... where student id=? and job id=? fetch first ? rows only    already applied? (-> 409)
                        4 insert into app.application (applied_at, job_id, status, student_id, version, id) values (?, ?, ?, ?, ?, default)
                        The unique constraint uq_application is the last guard if two requests race past step 3.""");
    }

    private static Example m4(Example e) {
        return e
                .inside("POST /api/applications, step by step", "", "text", """
                        1  Content-Type: application/json -> Spring picks the Jackson HttpMessageConverter.
                        2  @RequestBody: the JSON text becomes an ApplyRequest record.
                        3  @Valid runs BEFORE your method. Missing field -> MethodArgumentNotValidException -> 400, your code never runs, 0 SQL.
                        4  apply() calls the same ApplicationService as the HTML form.
                        5  ResponseEntity.created(uri) -> status 201 and the Location header; body -> JSON.""")
                .inside("The five statements of the successful request (this run)", "", "text", """
                        select job_posting where id=?                       exists, still open?
                        select student where email=?                        who is applying?
                        select a.id ... fetch first ? rows only             already applied?
                        insert into app.application (...) values (..., default)
                        select a.*, j.*, c.*, s.* ... where a.id=?          build the ApplicationRow for the response body""")
                .inside("Why 201 and not 200",
                        "201 Created plus Location tells a client program where the new thing lives, so it does not have to parse the body to find the id. 409 for 'already applied' and 404 for 'unknown email' come from the service exceptions, handled in M5.",
                        "", null)
                .inside("One rule, two doors", "", "text", """
                        HTML form  ApplicationController.apply     @Valid + BindingResult -> page, redirect
                        JSON API   ApplicationApiController.apply  @Valid @RequestBody    -> JSON, 201
                        Both call ApplicationService.apply(jobId, email). The rules exist in one place.""");
    }

    private static Example m5(Example e) {
        return e
                .inside("How an exception finds its handler", "", "text", """
                        1  Your service throws NotFoundException (a RuntimeException). The transaction rolls back.
                        2  The exception travels up to DispatcherServlet.
                        3  It asks its HandlerExceptionResolvers. ExceptionHandlerExceptionResolver goes first:
                              a) @ExceptionHandler methods inside the controller itself
                              b) then @ControllerAdvice / @RestControllerAdvice beans, in @Order
                        4  The first handler whose exception type matches wins.
                        5  Nothing matched -> DefaultHandlerExceptionResolver (standard 4xx) -> or the error page with a 500.""")
                .inside("Why one exception gives a page for people and JSON for programs", "", "text", """
                        GlobalExceptionHandler   @ControllerAdvice(annotations = Controller.class)        returns the view "error"
                        ApiExceptionHandler      @RestControllerAdvice(annotations = RestController.class)  returns ProblemDetail (JSON)
                                                 @Order(HIGHEST_PRECEDENCE) so it is asked first for REST controllers
                        Same NotFoundException, different advice, chosen by the kind of controller that threw it.""")
                .inside("What the empty-body request does", "", "text", """
                        POST /api/applications  {}
                          @Valid fails before the method -> MethodArgumentNotValidException
                          invalid() collects the field errors -> ProblemDetail 400 with an 'errors' object
                          0 SQL. The service never ran.""")
                .inside("The body the client receives (Content-Type: application/problem+json)", "", "json", """
                        {
                          "type": "about:blank",
                          "title": "Bad Request",
                          "status": 400,
                          "detail": "Validation failed",
                          "instance": "/api/applications",
                          "errors": { "jobId": "must not be null", "email": "must not be blank" }
                        }""");
    }

    private static Example m6(Example e) {
        return e
                .inside("Where an interceptor sits", "", "text", """
                        browser
                          -> servlet Filter                 knows only servlet request/response (no Spring controller)
                          -> DispatcherServlet
                          -> HandlerMapping                 finds the controller method AND the interceptors that apply
                          -> interceptor.preHandle          before the controller      (we note the SQL count and the time)
                          -> controller method
                          -> interceptor.postHandle         after the controller, before the view renders
                          -> view renders / JSON written
                          -> interceptor.afterCompletion    always called, even after an exception (we compute and log)""")
                .inside("The arithmetic", "", "text", """
                        preHandle:          before = stats.getPrepareStatementCount()      e.g. 120
                        afterCompletion:    after  = stats.getPrepareStatementCount()      e.g. 146
                        this request caused 146 - 120 = 26 SQL statements
                        Where does the counter come from? hibernate.generate_statistics=true in application.properties:
                        Hibernate counts every statement it prepares. The interceptor only reads it.""")
                .inside("Filter versus Interceptor versus ControllerAdvice", "", "text", """
                        Filter            servlet level, before Spring MVC. Logging, security, compression, CORS.
                        Interceptor       around the controller call, knows which handler runs. Timing, auth checks, our SQL counter.
                        ControllerAdvice  only for exceptions (and shared @ModelAttribute / binders). The error response.""")
                .inside("What we saw", "", "text", """
                        GET /applications?slow=true -> 200 | 26 SQL
                        GET /applications           -> 200 | 1 SQL
                        GET /students?branch=CE&minCgpa=8 -> 200 | 1 SQL
                        POST /jobs/1/apply (empty email)  -> 200 | 1 SQL
                        POST /api/applications ({})       -> 400 | 0 SQL""");
    }

    private static Example m7(Example e) {
        return e
                .inside("Why the raw entity fails: what Jackson meets", "", "text", """
                        1  findAll() returns JobPosting objects; each job.company is a lazy proxy (a generated subclass).
                        2  The transaction ended when the repository method returned (open-in-view=false), so the session is closed.
                        3  Jackson walks the getters. getCompany() -> proxy -> Jackson reads its properties -> the proxy tries to select.
                        4  No session -> LazyInitializationException, wrapped as HttpMessageNotWritableException.
                        5  Response: HTTP 500. The console says:
                           Could not write JSON: Could not initialize proxy [com.ppsu.placement.company.Company#1] - no session""")
                .inside("Even if the proxy loaded, there would be two more problems", "", "text", """
                        Company.postings -> List<JobPosting> -> each job.company -> postings -> ...   infinite recursion
                        The JSON would expose every column of the table (including ones you never meant to publish)
                        and freeze your table design into the API: rename a column and every client breaks.""")
                .inside("What the DTO changes", "", "text", """
                        JobView(id, title, companyId, company, city, minLpa, maxLpa, lastDate, status)
                        - built inside the service, while the session is open (the join fetch already loaded the company)
                        - flat: no proxies, no cycles
                        - exactly the fields the screen needs; the API can stay stable while the tables change""");
    }

    private static Example m8(Example e) {
        return e
                .inside("Where each next step plugs in", "", "text", """
                        Spring Security   a chain of servlet Filters in front of DispatcherServlet:
                                          request -> authenticate -> authorise (roles) -> your controller
                        Async requests    the controller returns Callable / DeferredResult; Tomcat frees its thread, a worker
                                          finishes the job, and the response is written later
                        Tests             MockMvc runs the real DispatcherServlet without a server (see ExamplesTest)
                        Monitoring        Actuator exposes /actuator/health now; add metrics (Micrometer) later
                        Deployment        mvnw package -> one executable jar -> Docker image -> CI""");
    }
}
