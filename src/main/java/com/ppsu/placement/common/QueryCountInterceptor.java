package com.ppsu.placement.common;

import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Example M6: logs, for every request, how many SQL statements it caused.
 * Relies on hibernate.generate_statistics=true. The counter is global, so with several users at once the
 * numbers mix: a teaching tool, not production monitoring.
 */
@Component
class QueryCountInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(QueryCountInterceptor.class);

    private final Statistics stats;
    private final RequestLog requestLog;

    QueryCountInterceptor(EntityManagerFactory emf, RequestLog requestLog) {
        this.stats = emf.unwrap(SessionFactory.class).getStatistics();
        this.requestLog = requestLog;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        req.setAttribute("qc.sql", stats.getPrepareStatementCount());
        req.setAttribute("qc.start", System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        Long sqlBefore = (Long) req.getAttribute("qc.sql");
        Long start = (Long) req.getAttribute("qc.start");
        if (sqlBefore == null || start == null) return;
        long sql = stats.getPrepareStatementCount() - sqlBefore;
        long ms = (System.nanoTime() - start) / 1_000_000;
        String uri = req.getQueryString() == null ? req.getRequestURI() : req.getRequestURI() + "?" + req.getQueryString();
        log.info("{} {} -> {} | {} SQL | {} ms", req.getMethod(), uri, res.getStatus(), sql, ms);
        requestLog.add(req.getMethod(), uri, res.getStatus(), sql, ms);
    }
}
