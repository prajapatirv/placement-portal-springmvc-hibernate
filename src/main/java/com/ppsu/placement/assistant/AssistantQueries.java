package com.ppsu.placement.assistant;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ppsu.placement.application.ApplicationStatus;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

/**
 * Read-only JPQL for the assistant. Scalar projections: one query each, no lazy loading,
 * and no changes to the existing repositories. The model never writes a query; we do.
 */
@Repository
@Transactional(readOnly = true)
@ConditionalOnProperty(name = "assistant.enabled", havingValue = "true")
public class AssistantQueries {

    private static final String APPLICATION_ROWS = """
            select s.name, c.name, j.title, a.status
            from Application a join a.student s join a.job j join j.company c
            """;

    @PersistenceContext
    private EntityManager em;

    public List<String> companyNames() {
        return em.createQuery("select c.name from Company c order by c.name", String.class).getResultList();
    }

    /** student, company, role, status */
    public List<Object[]> applicationsByStatus(ApplicationStatus status) {
        return em.createQuery(APPLICATION_ROWS + " where a.status = :status order by c.name, s.name", Object[].class)
                .setParameter("status", status)
                .getResultList();
    }

    /** student, company, role, status. Status is optional. */
    public List<Object[]> applicationsForCompany(String companyName, ApplicationStatus statusOrNull) {
        String jpql = APPLICATION_ROWS + " where lower(c.name) = lower(:company)"
                + (statusOrNull != null ? " and a.status = :status" : "")
                + " order by s.name";
        TypedQuery<Object[]> q = em.createQuery(jpql, Object[].class).setParameter("company", companyName);
        if (statusOrNull != null) q.setParameter("status", statusOrNull);
        return q.getResultList();
    }

    /** name, email, branch, cgpa */
    public List<Object[]> studentsWithoutApplications() {
        return em.createQuery("""
                select s.name, s.email, s.branch, s.cgpa from Student s
                where not exists (select 1 from Application a where a.student = s)
                order by s.name
                """, Object[].class).getResultList();
    }

    /** company, number of applications */
    public List<Object[]> applicationsPerCompany() {
        return em.createQuery("""
                select c.name, count(a) from Application a join a.job j join j.company c
                group by c.name order by count(a) desc, c.name
                """, Object[].class).getResultList();
    }

    public long studentCount() {
        return em.createQuery("select count(s) from Student s", Long.class).getSingleResult();
    }

    public long placedStudentCount() {
        return em.createQuery("select count(distinct a.student.id) from Application a where a.status = :s", Long.class)
                .setParameter("s", ApplicationStatus.SELECTED).getSingleResult();
    }

    /** status, number of applications */
    public List<Object[]> applicationCountsByStatus() {
        return em.createQuery("select a.status, count(a) from Application a group by a.status", Object[].class)
                .getResultList();
    }
}
