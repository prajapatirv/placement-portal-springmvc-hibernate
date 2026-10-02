package com.ppsu.placement;

import static org.assertj.core.api.Assertions.assertThat;

import com.ppsu.placement.application.ApplicationService;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** DEMO 4: the N+1 problem, proven with numbers. 26 JDBC statements with LAZY proxies, 1 with JOIN FETCH. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:qc;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS app")
@ActiveProfiles("local")
class QueryCountTest {

    @Autowired ApplicationService service;
    @Autowired EntityManagerFactory emf;

    Statistics stats;

    @BeforeEach
    void resetStatistics() {
        stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();
    }

    @Test
    void listSlow_fires_26_statements() {
        assertThat(service.listSlow()).hasSize(20);
        // 1 list + 8 students + 12 jobs + 5 companies
        assertThat(stats.getPrepareStatementCount()).isEqualTo(26);
    }

    @Test
    void listFast_fires_1_statement() {
        assertThat(service.listFast()).hasSize(20);
        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
    }
}
