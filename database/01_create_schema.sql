-- ============================================================================
-- STEP 1 (REQUIRED, run once): Supabase Dashboard > SQL Editor > New query > paste > Run
-- ============================================================================
-- Hibernate and Flyway do not create the schema themselves. Supabase exposes the
-- "public" schema through its Data API, so the application tables live in "app".
-- After this one statement, start the application (start.cmd / start.sh):
-- Flyway then creates the 4 tables and loads the seed data automatically
-- (5 companies, 12 job postings, 8 students, 20 applications).

create schema if not exists app;
