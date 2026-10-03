package com.ppsu.placement.application;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByStudentIdAndJobId(Long studentId, Long jobId);

    long countByStudentId(Long studentId);

    long countByJobId(Long jobId);

    // the N+1 fix: one query that brings everything the page needs
    @Query("""
            select a from Application a
              join fetch a.student
              join fetch a.job j
              join fetch j.company
            order by a.appliedAt desc, a.id desc
            """)
    List<Application> findAllWithDetails();

    @Query("""
            select a from Application a
              join fetch a.student
              join fetch a.job j
              join fetch j.company
            where a.id = :id
            """)
    Optional<Application> findWithDetailsById(@Param("id") Long id);

    // Example H3: "My applications", naive. No fetch plan, so every row triggers more selects (N+1).
    List<Application> findByStudentEmailIgnoreCase(String email);

    // Example H5: the same question with a fetch plan. One statement.
    @EntityGraph(attributePaths = {"student", "job", "job.company"})
    List<Application> findWithGraphByStudentEmailIgnoreCase(String email);

    // Example H8: one UPDATE for many rows. Bypasses the persistence context and does not touch @Version.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Application a set a.status = :status where a.id in :ids")
    int updateStatusIn(@Param("ids") Collection<Long> ids, @Param("status") ApplicationStatus status);
}
