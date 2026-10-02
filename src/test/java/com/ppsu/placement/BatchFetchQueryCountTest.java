package com.ppsu.placement;

import static org.assertj.core.api.Assertions.assertThat;

import com.ppsu.placement.application.ApplicationService;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** L6: default_batch_fetch_size softens the N+1 problem from 26 statements to 4 without touching any code. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:batch;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS app")
@ActiveProfiles({"local", "batch"})
class BatchFetchQueryCountTest {

    @Autowired ApplicationService service;
    @Autowired EntityManagerFactory emf;

    @Test
    void listSlow_with_batch_fetching_fires_4_statements() {
        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();
        service.listSlow();
        // 1 list + 1 batched load of students + 1 of jobs + 1 of companies
        assertThat(stats.getPrepareStatementCount()).isEqualTo(4);
    }
}
