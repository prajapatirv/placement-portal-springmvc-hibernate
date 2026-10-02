# Task 4: Prerequisites (software, versions, setup)

## Required versions

| Tool | Version | Notes |
| --- | --- | --- |
| **JDK** | **21 (LTS)**; 25 also works | the code uses records, text blocks, switch expressions. The same JDK on every machine. Verified with Temurin 21.0.12 |
| Build | Maven **wrapper** (`mvnw`, `mvnw.cmd`) in the repo | no Maven install needed; downloads Maven 3.9.16 on first run |
| Spring Boot | 3.5.16 (set in `pom.xml`) | Hibernate ORM 6.6.x, Spring Data JPA, Thymeleaf, Flyway |
| IDE | IntelliJ IDEA Community **or** VS Code (Extension Pack for Java + Spring Boot Extension Pack) | database tool window is Ultimate only; use the Supabase table editor |
| Git | 2.x + GitHub account | tags, fork, clone |
| Database | Supabase free project (PostgreSQL) **or** nothing (profile `local` uses in-memory H2) | |
| Optional | `curl` or Postman (JSON endpoint), Docker Desktop (local Postgres fallback), GitHub CLI `gh` | |

Needed disk: about 600 MB (JDK 200 MB, Maven repository 300 MB).

## 1. Install JDK 21

**Windows (no admin needed)**
1. Download the *Windows x64 zip* of Eclipse Temurin 21 from https://adoptium.net/temurin/releases/?version=21
2. Unzip to `C:\Users\<you>\.jdks\temurin-21`
3. Set user variables: `JAVA_HOME=C:\Users\<you>\.jdks\temurin-21` and add `%JAVA_HOME%\bin` to the user `Path` (or run `prerequisites\setup-windows.ps1`).
4. Open a **new** terminal: `java -version` must print `21`.

(Alternative with admin: `winget install EclipseAdoptium.Temurin.21.JDK`, or the MSI installer.)

**macOS**: `brew install --cask temurin@21`  **Linux (Debian/Ubuntu)**: `sudo apt install temurin-21-jdk` (after adding the Adoptium repository) or `sdk install java 21-tem` with SDKMAN.

## 2. Check everything

```powershell
./prerequisites/check-prereqs.ps1      # Windows PowerShell
```
```bash
./prerequisites/check-prereqs.sh       # macOS / Linux / Git Bash
```
Each script reports JDK (must be 21 or newer), Git, the Maven wrapper, `config/supabase.properties`, and port 8080.

## 3. Run

```powershell
# no accounts needed
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
# with Supabase
# first fill config\supabase.properties (section 5), then:
.\mvnw.cmd spring-boot:run
# or simply: start.cmd  (Supabase)   |   start.cmd local   |   stop.cmd
```
Open http://localhost:8080.

## 4. IDE setup (5 minutes)

Both IDEs, `start.cmd` and `start.sh` read the same git-ignored file **`config/supabase.properties`** (copy `config/supabase.properties.example`, fill in the password). Nothing is typed into IDE settings, so the secret exists in one place.

**IntelliJ IDEA**: open the folder (it detects `pom.xml`), trust the project; File > Project Structure > SDK = JDK 21. Shared run configurations from `.run/` appear automatically: **PlacementPortal Supabase** (uses `config/supabase.properties`) and **PlacementPortal local H2**. Working directory must be the project root (already set). Presentation mode / font 20 pt for the session.

**VS Code**: install the two extension packs, open the folder, choose JDK 21. Run and Debug shows the entries from `.vscode/launch.json`: Supabase (reads `config/supabase.properties`), local H2, and Supabase + N+1 demo.

Alternative without a file: Spring also accepts the same keys as environment variables (`SUPABASE_HOST`, `SUPABASE_USER`, `SUPABASE_PASSWORD`).

**Verify the connection:** start the Supabase configuration; the log shows `Database: jdbc:postgresql://...pooler.supabase.com` and `Successfully applied 3 migrations`. In Supabase > Table Editor, switch schema to `app`: `company`, `job_posting`, `student`, `application`, `flyway_schema_history`.

## 5. Supabase (once)

1. New project (region near the venue, e.g. Mumbai); keep the DB password in a password manager.
2. SQL Editor: `create schema if not exists app;`
3. Connect > **Session pooler** > JDBC string (port 5432). Never the Transaction pooler (6543) and not the IPv6-only direct string.
4. Append `&sslmode=require`; percent-encode special characters in the password (`&` = `%26`, `#` = `%23`, `?` = `%3F`, space = `%20`).
5. Put host, user and password into `config/supabase.properties` (copy the `.example`; the copy is git-ignored). The project URL and the publishable key are not used by this JDBC app.
6. After a public session: reset the password in Project Settings > Database.

## 6. Student pre-session message (copy for the coordinator)

> Please install before the session: JDK 21, IntelliJ IDEA Community or VS Code, Git, a free GitHub account and (optional) a free Supabase account. Run `prerequisites/check-prereqs` after cloning. No Supabase account? Use the `local` profile.
