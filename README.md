# Distributed Job Scheduling Platform

A distributed job scheduling system built with Java, Spring Boot, PostgreSQL, and Docker. The platform schedules jobs, processes them across multiple worker instances, and handles failures through configurable retry policies.

## Tech Stack

- Java 21
- Spring Boot
- Spring Data JPA and Hibernate
- PostgreSQL
- Docker and Docker Compose
- Maven
- JUnit

## Features

- REST APIs to create, retrieve, list, and filter jobs.
- Priority-based scheduling of eligible jobs.
- Concurrent job claiming using PostgreSQL transactions and `FOR UPDATE SKIP LOCKED`.
- Configurable retries with exponential backoff.
- Worker registration and heartbeat monitoring.
- Recovery of stale jobs after worker interruptions.
- Docker Compose setup for running multiple worker instances.
- Integration testing for REST API operations.

## Architecture

The application consists of an API service, a PostgreSQL database, and multiple worker instances.

1. Clients submit jobs through the REST API.
2. Jobs are stored in PostgreSQL.
3. Workers claim eligible jobs transactionally and execute them.
4. Job statuses and retry information are updated in the database.
5. Worker heartbeats and stale-job recovery help handle interruptions.

## Getting Started

### Prerequisites

- Java 21 or later
- Maven
- Docker Desktop

### Start the Application

From the project root, run:

```bash
docker compose up -d --build --scale worker=2
```

Check the running containers:

```bash
docker compose ps
```

The API runs at `http://localhost:8080`.

### API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/jobs` | Create a job |
| GET | `/api/jobs` | List jobs |
| GET | `/api/jobs/{id}` | Retrieve a job |
| GET | `/api/jobs/filter?status=SCHEDULED` | Filter jobs by status |
| GET | `/api/jobs/page?page=0&size=10` | List jobs with pagination |
| GET | `/api/workers` | List registered workers |

### Run Tests

```bash
mvn test
```

### Stop the Application

```bash
docker compose down
```

## Future Improvements

- Expand automated tests for validation, retries, and concurrent processing.
- Improve recovery and lease renewal for long-running jobs.
- Add metrics and monitoring.

## Project Status

A backend engineering project exploring distributed job processing, database concurrency, and worker reliability.