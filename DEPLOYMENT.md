# Deployment & Setup Guide - AI Bug Resolver

This guide provides step-by-step instructions for deploying and running the **AI Bug Resolver** application on a new machine or server environment.

---

## 🏗️ System Architecture Overview

```
┌─────────────────────────┐      HTTP REST      ┌──────────────────────────────────────────┐
│   React 18 + Vite UI    │ ──────────────────> │        Spring Boot 3.2 Backend           │
│   (Port 5173 / static)  │ <────────────────── │          (Port 8080 /api)                │
└─────────────────────────┘                     └────────────────────┬─────────────────────┘
                                                                     │
                                         ┌───────────────────────────┼───────────────────────────┐
                                         │                           │                           │
                                         ▼                           ▼                           ▼
                             ┌───────────────────────┐   ┌───────────────────────┐   ┌───────────────────────┐
                             │  PostgreSQL / H2 DB   │   │   OpenAI API (GPT-4)  │   │  Jira & Bitbucket API │
                             └───────────────────────┘   └───────────────────────┘   └───────────────────────┘
```

---

## 📋 System Prerequisites

Before deploying the application, ensure the host system has the following software installed:

| Software | Version Required | Download Link / Notes |
|----------|------------------|-----------------------|
| **Java JDK** | 17 or higher | [OpenJDK 17](https://adoptium.net/) / Oracle JDK 17 |
| **Node.js** | 18.x or higher | [Node.js Downloads](https://nodejs.org/) (includes `npm`) |
| **Maven** | 3.8+ | [Apache Maven](https://maven.apache.org/) (or build via IDE) |
| **PostgreSQL** | 14.x or higher | *(Optional if using H2 dev mode)* [PostgreSQL Downloads](https://www.postgresql.org/download/) |

---

## 📁 Package Contents

When extracted from `AI-App.zip`, the project structure will look like this:

```
AI-App/
├── backend/                      # Spring Boot backend application
│   ├── pom.xml                   # Maven dependencies and configuration
│   └── src/                      # Java source code & SQL migration scripts
├── frontend/                     # React + Vite frontend application
│   ├── package.json              # NPM dependencies and scripts
│   ├── vite.config.js            # Vite configuration
│   └── src/                      # React UI components and state
├── DEPLOYMENT.md                 # Deployment instructions (this file)
└── README.md                     # Project overview and features
```

---

## 🚀 Step-by-Step Deployment Guide

### Step 1: Extract Project Files

Extract `AI-App.zip` to your target directory on the host machine:

- **Linux / macOS**:
  ```bash
  unzip AI-App.zip -d ~/AI-App
  cd ~/AI-App
  ```
- **Windows (PowerShell)**:
  ```powershell
  Expand-Archive -Path .\AI-App.zip -DestinationPath C:\Projects\AI-App
  cd C:\Projects\AI-App
  ```

---

### Step 2: Database Setup

You can choose between **PostgreSQL (Production)** or **H2 (Development/Quick Start)**.

#### Option A: PostgreSQL Database (Recommended for Production)

1. Open PostgreSQL shell (`psql`) or your database management tool (pgAdmin, DBeaver):
   ```sql
   CREATE DATABASE ai_bug_resolver;
   CREATE USER postgres WITH PASSWORD 'postgres';
   GRANT ALL PRIVILEGES ON DATABASE ai_bug_resolver TO postgres;
   ```
2. Database tables will automatically be created and managed by **Flyway** migrations when the backend starts.

#### Option B: H2 In-Memory Database (Zero-Config Quick Start)

No database installation required! You can launch the backend with the `dev` profile to use an embedded H2 database.

---

### Step 3: Backend Configuration & Launch

#### 1. Environment Variables Reference

Configure the backend using environment variables or by passing parameters:

| Variable Name | Description | Default Value | Required? |
|---------------|-------------|---------------|-----------|
| `DB_URL` | JDBC database URL | `jdbc:postgresql://localhost:5432/ai_bug_resolver` | No |
| `DB_USERNAME` | Database username | `postgres` | No |
| `DB_PASSWORD` | Database password | `postgres` | No |
| `AI_PROVIDER` | AI service provider | `openai` | No |
| `AI_API_KEY` | OpenAI API Key | *(empty)* | **Yes (for AI resolution)** |
| `AI_MODEL` | OpenAI Model | `gpt-4` | No |
| `JIRA_BASE_URL` | Jira instance URL | *(empty)* | Optional (can configure in UI) |
| `JIRA_USERNAME` | Jira account email | *(empty)* | Optional (can configure in UI) |
| `JIRA_API_TOKEN` | Jira API Token | *(empty)* | Optional (can configure in UI) |
| `BITBUCKET_USERNAME` | Bitbucket username | *(empty)* | Optional (can configure in UI) |
| `BITBUCKET_APP_PASSWORD` | Bitbucket App Password | *(empty)* | Optional (can configure in UI) |

#### 2. Running the Backend

Navigate to the `backend` directory:
```bash
cd backend
```

##### Option A: Run with PostgreSQL (Default Profile)

* **Linux / macOS**:
  ```bash
  export DB_URL=jdbc:postgresql://localhost:5432/ai_bug_resolver
  export DB_USERNAME=postgres
  export DB_PASSWORD=postgres
  export AI_API_KEY=sk-your-openai-api-key-here
  
  mvn clean package -DskipTests
  java -jar target/ai-bug-resolver-1.0.0.jar
  ```

* **Windows (PowerShell)**:
  ```powershell
  $env:DB_URL="jdbc:postgresql://localhost:5432/ai_bug_resolver"
  $env:DB_USERNAME="postgres"
  $env:DB_PASSWORD="postgres"
  $env:AI_API_KEY="sk-your-openai-api-key-here"
  
  mvn clean package -DskipTests
  java -jar target/ai-bug-resolver-1.0.0.jar
  ```

##### Option B: Quick Run with Embedded H2 Database (Dev Profile)

* **Linux / macOS**:
  ```bash
  export AI_API_KEY=sk-your-openai-api-key-here
  mvn spring-boot:run -Dspring-boot.run.profiles=dev
  ```

* **Windows (PowerShell)**:
  ```powershell
  $env:AI_API_KEY="sk-your-openai-api-key-here"
  mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
  ```

*Backend REST API will start on **`http://localhost:8080/api`**.*

---

### Step 4: Frontend Configuration & Launch

Navigate to the `frontend` directory in a new terminal window:
```bash
cd frontend
```

#### 1. Install Dependencies
```bash
npm install
```

#### 2. Launch Development Server
```bash
npm run dev
```
*Frontend UI will open at **`http://localhost:5173`**.*

#### 3. Build static production bundle (Optional)
```bash
npm run build
```
The production bundle will be generated in `frontend/dist/`. You can serve it using `npx serve -s dist` or Nginx/Apache.

---

## 🌐 Verifying the Deployment

Once both services are running, open your browser and verify:

1. **Frontend Web UI**: Open [`http://localhost:5173`](http://localhost:5173)
2. **Backend Health / API**: Open [`http://localhost:8080/api/v1/analyses/`](http://localhost:8080/api/v1/analyses/)
3. **Interactive Swagger UI**: Open [`http://localhost:8080/api/swagger-ui.html`](http://localhost:8080/api/swagger-ui.html)
4. **H2 Database Console** *(if running in dev mode)*: Open [`http://localhost:8080/api/h2-console`](http://localhost:8080/api/h2-console) (JDBC URL: `jdbc:h2:mem:ai_bug_resolver`, Username: `sa`, Password: leave blank).

---

## 🛠️ Troubleshooting & Common Fixes

| Issue | Root Cause | Fix |
|-------|------------|-----|
| `Port 8080 in use` | Another process is using port 8080 | Change port in `backend/src/main/resources/application.yml` under `server.port` or kill the process using port 8080. |
| `Port 5173 in use` | Another process is using Vite port | Vite will automatically prompt/switch to `5174` or specify `npm run dev -- --port 3000`. |
| `Connection refused: postgresql` | PostgreSQL service is stopped or credentials wrong | Ensure PostgreSQL service is running (`systemctl status postgresql` or Windows Services) and check `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`. |
| `Flyway Migration Error` | Database schema mismatch | Drop database and recreate (`dropdb ai_bug_resolver && createdb ai_bug_resolver`) or set `spring.flyway.baseline-on-migrate=true`. |
| `OpenAI API 401 Unauthorized` | Missing or invalid API key | Set `AI_API_KEY` environment variable correctly before launching backend. |
| `CORS Error in Web Console` | Frontend requesting backend from different origin | Cross-Origin resource sharing is allowed by default in Spring Boot configuration (`WebConfig.java`). Ensure backend is running at `http://localhost:8080/api`. |

---

## 📄 License & Support

For issues, configurations, or extensions, consult the included `README.md` or contact your project administrator.
