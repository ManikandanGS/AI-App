# AI Bug Resolver

A web application that connects Jira, Bitbucket, and AI to automatically analyze and suggest fixes for bugs reported in Jira tickets.

## Architecture

```
┌──────────────┐     ┌─────────────────────────────────────────┐     ┌─────────────┐
│   React UI   │────▶│           Spring Boot Backend            │────▶│  PostgreSQL  │
│  (Vite +     │     │                                         │     │             │
│  Tailwind)   │◀────│  Jira API  │ Bitbucket API │  OpenAI    │◀────│             │
└──────────────┘     └─────────────────────────────────────────┘     └─────────────┘
```

## Features

- **Jira Integration**: Fetch bug ticket details, descriptions, comments
- **Bitbucket Integration**: Retrieve relevant source code for context
- **AI Analysis**: Send bug context + code to AI for root cause analysis and fix suggestions
- **Feedback Loop**: Rate AI resolutions (1-5 stars) to improve future analyses
- **Configuration Management**: Per-project Jira/Bitbucket/DB credentials
- **Dashboard**: Overview stats, recent analyses, feedback metrics

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2, Maven |
| Frontend | React 18, Vite, Tailwind CSS |
| Database | PostgreSQL |
| AI | OpenAI GPT-4 (pluggable) |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Migrations | Flyway |

## Prerequisites

- Java 17+
- Node.js 18+
- PostgreSQL 14+
- Maven 3.8+
- OpenAI API key (or other supported AI provider)

## Quick Start

### 1. Database Setup

```bash
# Create PostgreSQL database
createdb ai_bug_resolver
```

### 2. Backend Setup

```bash
cd backend

# Set environment variables (or edit application.yml)
export DB_URL=jdbc:postgresql://localhost:5432/ai_bug_resolver
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export AI_API_KEY=your-openai-api-key
export JIRA_BASE_URL=https://your-domain.atlassian.net
export JIRA_USERNAME=your-email@example.com
export JIRA_API_TOKEN=your-jira-api-token
export BITBUCKET_USERNAME=your-username
export BITBUCKET_APP_PASSWORD=your-app-password

# Build and run
mvn clean install
mvn spring-boot:run
```

The backend starts at `http://localhost:8080/api`

### 3. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Start dev server
npm run dev
```

The frontend starts at `http://localhost:5173`

### 4. Access the Application

- **App**: http://localhost:5173
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **API Docs**: http://localhost:8080/api/v3/api-docs

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/ai_bug_resolver` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `postgres` |
| `AI_PROVIDER` | AI provider (openai) | `openai` |
| `AI_API_KEY` | AI provider API key | — |
| `AI_MODEL` | AI model name | `gpt-4` |
| `AI_MAX_TOKENS` | Max response tokens | `4096` |
| `AI_TEMPERATURE` | AI temperature (0-1) | `0.3` |
| `JIRA_BASE_URL` | Jira instance URL | — |
| `JIRA_USERNAME` | Jira username/email | — |
| `JIRA_API_TOKEN` | Jira API token | — |
| `BITBUCKET_BASE_URL` | Bitbucket API base URL | `https://api.bitbucket.org/2.0` |
| `BITBUCKET_USERNAME` | Bitbucket username | — |
| `BITBUCKET_APP_PASSWORD` | Bitbucket app password | — |

### Per-Project Configuration

You can also configure Jira and Bitbucket credentials per-project via the Configuration panel in the UI or the `/api/v1/config` API.

## API Endpoints

### Analysis
- `POST /api/v1/analyses/` — Submit ticket for AI analysis
- `GET /api/v1/analyses/` — List all analyses
- `GET /api/v1/analyses/{id}` — Get analysis details

### Feedback
- `POST /api/v1/feedback/` — Submit rating & feedback
- `GET /api/v1/feedback/resolution/{id}` — Get feedback for resolution
- `GET /api/v1/feedback/stats` — Aggregate statistics

### Configuration
- `POST /api/v1/config/` — Create project config
- `GET /api/v1/config/` — List all configs
- `GET /api/v1/config/{id}` — Get specific config
- `PUT /api/v1/config/{id}` — Update config
- `DELETE /api/v1/config/{id}` — Delete config

## How It Works

1. **Submit a Jira ticket ID** through the UI
2. **Backend fetches ticket details** from Jira (summary, description, comments)
3. **Backend searches Bitbucket** for relevant source files using keywords from the bug report
4. **AI model analyzes** the bug context + code and suggests a fix with root cause, code diff, and confidence score
5. **User reviews** the analysis and provides a rating (1-5 stars)
6. **Feedback loop**: Highly-rated fixes become few-shot examples in future prompts; poorly-rated fixes become anti-patterns to avoid

## Project Structure

```
ai-bug-resolver/
├── backend/                        # Spring Boot application
│   ├── pom.xml
│   └── src/main/java/com/airesolver/
│       ├── config/                 # Security, CORS, OpenAPI, RestTemplate
│       ├── controller/             # REST API endpoints
│       ├── exception/              # Custom exceptions & global handler
│       ├── model/
│       │   ├── dto/                # Request/Response records
│       │   ├── entity/             # JPA entities
│       │   └── enums/              # Status & Severity enums
│       ├── repository/             # Spring Data JPA repositories
│       └── service/                # Business logic & integrations
├── frontend/                       # React application
│   └── src/
│       ├── components/             # UI components
│       │   ├── common/             # Reusable (Badge, Modal, Spinner)
│       │   ├── Dashboard.jsx
│       │   ├── AnalysisForm.jsx
│       │   ├── AnalysisResult.jsx
│       │   ├── FeedbackPanel.jsx
│       │   ├── ConfigPanel.jsx
│       │   └── HistoryTable.jsx
│       └── services/api.js         # Axios API client
└── README.md
```

## License

MIT
