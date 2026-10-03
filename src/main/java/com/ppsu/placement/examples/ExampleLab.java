package com.ppsu.placement.examples;

import com.ppsu.placement.application.Application;
import com.ppsu.placement.application.ApplicationService;
import com.ppsu.placement.application.ApplicationStatus;
import com.ppsu.placement.student.Student;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Live demonstrations for the session. Each method runs inside a transaction that is ROLLED BACK at the end,
 * so the data is the same before and after and every demo can be repeated as often as needed.
 */
@Service
class ExampleLab {
    record Step(String action, String state, String evidence) {}

    @PersistenceContext
    private EntityManager em;

    private final TransactionTemplate tx;
    private final Statistics stats;
    private final JdbcTemplate jdbc;
    private final ApplicationService applications;

    ExampleLab(PlatformTransactionManager txManager, EntityManagerFactory emf, JdbcTemplate jdbc,
               ApplicationService applications) {
        this.tx = new TransactionTemplate(txManager);
        this.stats = emf.unwrap(SessionFactory.class).getStatistics();
        this.jdbc = jdbc;
        this.applications = applications;
    }

    private long statements() {
        return stats.getPrepareStatementCount();
    }

    /** Runs the work in a transaction and always rolls it back. */
    private <T> T rolledBack(Supplier<T> work) {
        return tx.execute(status -> {
            try {
                return work.get();
            } finally {
                status.setRollbackOnly();
            }
        });
    }

    // ---- H1: the mapping and the schema must agree -------------------------------------------------------

    Map<String, Object> mapping() {
        List<Map<String, Object>> entity = new ArrayList<>();
        for (Field f : Student.class.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            Column c = f.getAnnotation(Column.class);
            String column = (c != null && !c.name().isEmpty()) ? c.name() : f.getName();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("javaField", f.getName());
            row.put("javaType", f.getType().getSimpleName());
            row.put("column", column);
            row.put("nullable", c == null || c.nullable());
            entity.add(row);
        }
        List<Map<String, Object>> database = jdbc.queryForList("""
                select column_name, data_type, is_nullable from information_schema.columns
                where lower(table_schema) = 'app' and lower(table_name) = 'student'
                order by ordinal_position""");
        List<String> dbNames = database.stream()
                .map(r -> String.valueOf(r.get("column_name")).toLowerCase()).toList();
        List<String> missing = entity.stream().map(r -> (String) r.get("column"))
                .filter(c -> !dbNames.contains(c.toLowerCase())).toList();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("entity", "Student");
        out.put("mappedFields", entity);
        out.put("databaseColumns", database);
        out.put("columnsMissingInDatabase", missing);
        out.put("verdict", missing.isEmpty()
                ? "Mapping and schema agree: Hibernate validate lets the application start."
                : "Mapping refers to columns that do not exist: Hibernate validate would refuse to start.");
        return out;
    }

    // ---- H6: persistence context ------------------------------------------------------------------------

    Map<String, Object> lifecycle() {
        return rolledBack(() -> {
            List<Step> steps = new ArrayList<>();
            Student s = new Student("Lifecycle Demo", "life@ppsu.example", "CE", new BigDecimal("8.0"));
            steps.add(new Step("new Student(...)", "TRANSIENT", "em.contains(s) = " + em.contains(s)
                    + ", id = " + s.getId() + " (Hibernate does not know it)"));

            long before = statements();
            em.persist(s);
            steps.add(new Step("em.persist(s)", "PERSISTENT", "em.contains(s) = " + em.contains(s) + ", id = "
                    + s.getId() + ", " + (statements() - before) + " statement (IDENTITY: the insert ran now)"));

            em.detach(s);
            s.setName("Changed while detached");
            steps.add(new Step("em.detach(s); s.setName(...)", "DETACHED", "em.contains(s) = " + em.contains(s)
                    + " (changes to s are no longer tracked)"));

            before = statements();
            Student reloaded = em.find(Student.class, s.getId());
            steps.add(new Step("em.find(Student.class, id)", "PERSISTENT (a new object)",
                    "name in database = \"" + reloaded.getName() + "\", same object as s? " + (reloaded == s)
                            + ", " + (statements() - before) + " select"));

            before = statements();
            em.remove(reloaded);
            em.flush();
            steps.add(new Step("em.remove(reloaded)", "REMOVED", "em.contains = " + em.contains(reloaded)
                    + ", " + (statements() - before) + " delete at flush/commit"));

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("steps", steps);
            out.put("note", "Everything ran in a transaction that was rolled back: the database is unchanged.");
            return out;
        });
    }

    Map<String, Object> firstLevelCache() {
        return rolledBack(() -> {
            Long id = firstApplicationIds(1, null).get(0);
            long before = statements();
            Application first = em.find(Application.class, id);
            Application second = em.find(Application.class, id);
            long afterTwo = statements() - before;
            boolean same = first == second;
            em.clear();
            long beforeThird = statements();
            Application third = em.find(Application.class, id);
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("applicationId", id);
            out.put("twoFindsInOneTransaction", Map.of("statements", afterTwo, "sameObject", same));
            out.put("afterEmClear", Map.of("statements", statements() - beforeThird,
                    "sameObjectAsFirst", third == first));
            out.put("say", "One object per row per session. This is the first-level cache; you cannot turn it off.");
            return out;
        });
    }

    Map<String, Object> dirtyChecking() {
        return rolledBack(() -> {
            Long id = firstApplicationIds(1, ApplicationStatus.APPLIED).get(0);
            Application a = em.find(Application.class, id);
            ApplicationStatus statusBefore = a.getStatus();
            Long versionBefore = a.getVersion();
            a.setStatus(ApplicationStatus.SHORTLISTED);                  // no save() anywhere
            long before = statements();
            em.flush();                                                  // what commit would do
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("applicationId", id);
            out.put("statusBefore", statusBefore);
            out.put("statusAfter", a.getStatus());
            out.put("versionBefore", versionBefore);
            out.put("versionAfter", a.getVersion());
            out.put("statementsAtFlush", statements() - before);
            out.put("sql", "update application set status=?, version=? where id=? and version=?  (see the console)");
            out.put("say", "Hibernate kept a snapshot, compared at flush, and wrote the difference. No save() call.");
            return out;
        });
    }

    // ---- H8: loop versus one UPDATE ---------------------------------------------------------------------

    Map<String, Object> bulkCompare() {
        List<Long> ids = firstApplicationIds(3, ApplicationStatus.APPLIED);

        Map<String, Object> loop = rolledBack(() -> {
            Map<Long, Long> before = versions(ids);
            long s0 = statements();
            applications.bulkShortlist(ids);                             // findById + dirty checking, per id
            em.flush();
            return result(statements() - s0, before, versions(ids));
        });
        Map<String, Object> bulk = rolledBack(() -> {
            Map<Long, Long> before = versions(ids);
            long s0 = statements();
            int updated = applications.bulkShortlistFast(ids);           // one JPQL update
            Map<String, Object> r = result(statements() - s0, before, versions(ids));
            r.put("rowsUpdated", updated);
            return r;
        });

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ids", ids);
        out.put("loopWithDirtyChecking", loop);
        out.put("oneBulkUpdate", bulk);
        out.put("lesson", "The bulk update is one statement, but @Version did not move: optimistic locking no longer "
                + "protects those rows. It also skips entity callbacks and business rules.");
        out.put("note", "Both runs were rolled back: the data is unchanged.");
        return out;
    }

    private Map<String, Object> result(long statements, Map<Long, Long> before, Map<Long, Long> after) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("statements", statements);
        r.put("versionsBefore", before);
        r.put("versionsAfter", after);
        r.put("versionIncremented", !before.equals(after));
        return r;
    }

    // ---- helpers ----------------------------------------------------------------------------------------

    /** Scalar query: reads versions from the database without loading entities into the persistence context. */
    private Map<Long, Long> versions(List<Long> ids) {
        Map<Long, Long> out = new LinkedHashMap<>();
        em.createQuery("select a.id, a.version from Application a where a.id in :ids order by a.id", Object[].class)
                .setParameter("ids", ids).getResultList().forEach(row -> out.put((Long) row[0], (Long) row[1]));
        return out;
    }

    private List<Long> firstApplicationIds(int count, ApplicationStatus status) {
        String jpql = status == null ? "select a.id from Application a order by a.id"
                : "select a.id from Application a where a.status = :s order by a.id";
        var q = em.createQuery(jpql, Long.class).setMaxResults(count);
        if (status != null) q.setParameter("s", status);
        return q.getResultList();
    }
}
