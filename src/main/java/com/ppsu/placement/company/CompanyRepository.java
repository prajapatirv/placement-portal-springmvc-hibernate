package com.ppsu.placement.company;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    // one grouped query for the list page (a count per company would be another N+1)
    @Query("""
            select new com.ppsu.placement.company.CompanyView(c.id, c.name, c.city, c.website, count(j))
            from Company c left join c.postings j
            group by c.id, c.name, c.city, c.website
            order by c.name
            """)
    List<CompanyView> findAllWithJobCount();
}
