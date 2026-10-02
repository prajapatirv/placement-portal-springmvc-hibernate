@echo off
rem One-click start: start.cmd [supabase|local|demo] [-Port 8081] [-Rebuild]
rem   start.cmd        Supabase (config\supabase.properties)
rem   start.cmd local  in-memory H2, no account needed
rem   start.cmd demo   Supabase + N+1 demo output in logs\app.log
setlocal
set "MODE=supabase"
set "EXTRA="
if /i "%~1"=="local" (set "MODE=local" & shift)
if /i "%~1"=="supabase" (set "MODE=supabase" & shift)
if /i "%~1"=="demo" (set "MODE=demo" & shift)
:collect
if "%~1"=="" goto run
set "EXTRA=%EXTRA% %1"
shift
goto collect
:run
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\start.ps1" -Mode %MODE% %EXTRA%
exit /b %ERRORLEVEL%
