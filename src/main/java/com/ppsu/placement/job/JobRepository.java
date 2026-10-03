package com.ppsu.placement.job;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobRepository extends JpaRepository<JobPosting, Long> {

    // list page: fetching the company in the same query avoids another N+1
    @Query("select j from JobPosting j join fetch j.company "
         + "where j.status = com.ppsu.placement.job.JobStatus.OPEN order by j.lastDate")
    List<JobPosting> findOpenWithCompany();

    // student challenge: filter by city, still exactly one select
    @Query("select j from JobPosting j join fetch j.company c "
         + "where lower(c.city) = lower(:city) "
         + "and j.status = com.ppsu.placement.job.JobStatus.OPEN order by j.lastDate")
    List<JobPosting> findOpenByCity(@Param("city") String city);

    // trace slide: the detail page is one join
    @Query("select j from JobPosting j join fetch j.company where j.id = :id")
    Optional<JobPosting> findWithCompanyById(@Param("id") Long id);

    // L6 paging: a to-one fetch is safe with paging (a collection fetch is not)
    @EntityGraph(attributePaths = "company")
    Page<JobPosting> findByStatus(JobStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "company")
    Page<JobPosting> findAllBy(Pageable pageable);

    // L6 projection: two columns in the SQL instead of the whole entity
    @Query("select j.title as title, j.company.name as companyName from JobPosting j where j.status = :status")
    List<JobSummary> summaries(@Param("status") JobStatus status);

    // Example H4: aggregate projection. left join keeps jobs with zero applicants (count 0).
    @Query("""
            select j.title as title, count(a) as applicants
            from JobPosting j left join Application a on a.job = j
            group by j.title
            order by count(a) desc, j.title
            """)
    List<JobApplicantCount> applicantsPerJob();

    // Example M1: a numeric request parameter, converted by Spring, answered by a derived query
    @EntityGraph(attributePaths = "company")
    List<JobPosting> findByMinPackageLpaGreaterThanEqualOrderByMinPackageLpaDesc(BigDecimal minLpa);

    long countByCompanyId(Long companyId);

    boolean existsByTitleIgnoreCase(String title);

    boolean existsByTitleIgnoreCaseAndIdNot(String title, Long id);
}
