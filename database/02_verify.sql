-- ============================================================================
-- VERIFY (run in Supabase SQL Editor after the application started once)
-- ============================================================================

-- 1. Row counts: expect 5, 12, 8, 20
select 'company' as table_name, count(*) as rows from app.company
union all select 'job_posting', count(*) from app.job_posting
union all select 'student',     count(*) from app.student
union all select 'application', count(*) from app.application;

-- 2. Flyway history: expect versions 1, 2, 3 with success = true
select installed_rank, version, description, success, installed_on
from app.flyway_schema_history
order by installed_rank;

-- 3. Applications with names (what the N+1 demo reads): expect 20 rows
select a.id, s.name as student, j.title as job, c.name as company, a.status, a.version
from app.application a
join app.student s      on s.id = a.student_id
join app.job_posting j  on j.id = a.job_id
join app.company c      on c.id = j.company_id
order by a.id;

-- 4. Index used by the open-jobs query (optional, L6). With 12 rows PostgreSQL will still choose a sequential scan.
explain analyze
select * from app.job_posting where status = 'OPEN' order by last_date;
