-- ============================================================================
-- RESET (optional, DESTRUCTIVE): wipes everything the app created and gives a clean start
-- ============================================================================
-- Use it before a rehearsal or the live session so the demo data is exactly
-- 5 / 12 / 8 / 20 and the N+1 demo prints 26 statements again.
-- It drops ONLY the "app" schema (tables, data and the Flyway history inside it).
-- Nothing in "public" or in Supabase's own schemas is touched.
-- After running it, start the application: Flyway recreates tables and seed data.

drop schema if exists app cascade;
create schema app;
