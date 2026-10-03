package com.ppsu.placement.student;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    // Example H2: the method name is the query (no body, no @Query)
    List<Student> findByBranchIgnoreCaseAndCgpaGreaterThanEqualOrderByCgpaDesc(String branch, BigDecimal minCgpa);
}
