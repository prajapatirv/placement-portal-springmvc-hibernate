package com.ppsu.placement.application;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
