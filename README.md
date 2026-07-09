# InRideMart

Enterprise-grade AI-powered in-ride commerce platform.

This repository is organized as a modular monorepo. The first production-ready slice is the `auth-service`; the remaining bounded contexts are documented and scaffolded so they can be implemented one module at a time with the same architecture.

## Current Implementation Status

| Module | Status |
| --- | --- |
| Authentication | Implemented |
| Customer | Planned |
| Driver | Planned |
| Merchant | Planned |
| Ride Booking | Planned |
| Product Catalog | Planned |
| Shopping Cart | Planned |
| Orders | Planned |
| Inventory | Planned |
| AI Recommendation | Planned |
| Payments | Planned |
| Notifications | Planned |
| Analytics | Planned |
| Admin Dashboard | Planned |
| Fraud Detection | Planned |

## Quick Start

The project currently has one implemented application service: `auth-service`.
The remaining modules are planned and documented, but are not runnable services yet.

### Prerequisites

- Java 21 or later
- Maven
- Docker Desktop
- Docker Compose

Verify the tools from PowerShell:

```powershell
java -version
mvn -version
docker --version
docker compose version
```

If Docker Desktop is installed but PowerShell cannot find `docker`, temporarily add Docker to the current terminal PATH:

```powershell
$env:Path += ";C:\Program Files\Docker\Docker\resources\bin"
```

For a permanent fix, add this folder to the Windows Environment Variables PATH:

```text
C:\Program Files\Docker\Docker\resources\bin
```

### 1. Start Infrastructure Services

From the project root:

```powershell
cd D:\InRideMart
docker compose up -d
```

This starts:

- PostgreSQL on `5432`
- Redis on `6379`
- Kafka on `9092`

Check that the containers are running:

```powershell
docker ps
```

### 2. Run Auth Service

Run the Spring Boot service from the project root:

```powershell
cd D:\InRideMart
mvn -pl services/auth-service spring-boot:run
```

Or run it from the service folder:

```powershell
cd D:\InRideMart\services\auth-service
mvn spring-boot:run
```

The application starts on:

```text
http://localhost:8081
```

### 3. Verify the Application

Health check:

```text
http://localhost:8081/actuator/health
```

Swagger UI:

```text
http://localhost:8081/swagger-ui.html
```

or:

```text
http://localhost:8081/swagger-ui/index.html
```

### 4. Stop Services

Stop the Spring Boot service with `Ctrl + C` in the terminal where it is running.

Stop Docker infrastructure:

```powershell
cd D:\InRideMart
docker compose down
```

Note: run the Spring Boot service with Maven on your machine for local development. The current `application.yml` points to `localhost` for PostgreSQL, Redis, and Kafka, which matches the ports exposed by `docker-compose.yml`.

## Architecture

Read the platform design package in [docs/system-design.md](docs/system-design.md).



# InRideMart - Local Development Setup Guide

This document describes how to set up the local development environment for the InRideMart microservices project and documents the issues encountered during the initial setup.

---

# Tech Stack

- Java 21
- Spring Boot 3.5.x
- PostgreSQL 18
- Redis 7
- Apache Kafka
- Docker Desktop
- Docker Compose
- Maven
- Flyway
- Spring Security
- Swagger / OpenAPI

---

# Project Structure

```
InRideMart
│
├── services
│   ├── auth-service
│   ├── ride-service
│   ├── payment-service
│   └── ...
│
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

# Initial Problems Encountered

## 1. PostgreSQL Authentication Failure

### Error

```
FATAL: password authentication failed for user "inridemart"
```

### Cause

- Database user was not created.
- Password in `application.yml` did not match PostgreSQL.
- Database did not exist.

### Solution

Login to PostgreSQL as postgres.

```sql
CREATE USER inridemart WITH PASSWORD 'InRide@123';

CREATE DATABASE inridemart_auth OWNER inridemart;

GRANT ALL PRIVILEGES ON DATABASE inridemart_auth TO inridemart;
```

Grant schema permissions.

```sql
\c inridemart_auth

GRANT ALL ON SCHEMA public TO inridemart;

ALTER SCHEMA public OWNER TO inridemart;
```

---

# 2. psql Command Not Found

### Error

```
psql : The term 'psql' is not recognized...
```

### Cause

PostgreSQL bin directory was not added to Windows PATH.

### Temporary Solution

```powershell
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres
```

### Permanent Solution

Add

```
C:\Program Files\PostgreSQL\18\bin
```

to the Windows Environment PATH.

---

# 3. Docker Command Not Found

### Error

```
docker : The term 'docker' is not recognized...
```

### Cause

Docker Desktop was installed but not available in PATH.

### Temporary Fix

```powershell
$env:Path += ";C:\Program Files\Docker\Docker\resources\bin"
```

### Permanent Fix

Add

```
C:\Program Files\Docker\Docker\resources\bin
```

to Windows Environment Variables.

Verify installation.

```powershell
docker --version

docker info
```

---

# 4. Kafka Image Not Found

### Error

```
manifest for bitnami/kafka:3.7 not found
```

### Cause

Old Docker image tag.

### Solution

Use a supported Kafka image.

```
apache/kafka:3.9.1
```

instead of

```
bitnami/kafka:3.7
```

---

# 5. Redis Connection Refused

### Error

```
Unable to connect to Redis

Connection refused localhost:6379
```

### Cause

Redis server was not running.

### Solution

Start Redis using Docker Compose.

---

# 6. Port Already In Use

### Error

```
Port 8081 was already in use.
```

### Find Process

```powershell
netstat -ano | findstr :8081
```

Identify the PID.

```powershell
tasklist /FI "PID eq <PID>"
```

Kill the process.

```powershell
taskkill /PID <PID> /F
```

---

# Docker Installation

Install Docker Desktop.

Verify installation.

```powershell
docker --version

docker compose version
```

Verify engine.

```powershell
docker info
```

---

# Docker Compose

Project contains

```
docker-compose.yml
```

Run all infrastructure.

```powershell
cd D:\InRideMart

docker compose up -d
```

Stop everything.

```powershell
docker compose down
```

View running containers.

```powershell
docker ps
```

---

# Services Started

Current infrastructure consists of

- PostgreSQL
- Redis
- Apache Kafka

---

# PostgreSQL

Database

```
inridemart_auth
```

User

```
inridemart
```

Password

```
InRide@123
```

Connection URL

```
jdbc:postgresql://localhost:5432/inridemart_auth
```

---

# Redis

Default Port

```
6379
```

Spring Boot configuration

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

---

# Kafka

Default Port

```
9092
```

Spring Boot configuration

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

---

# Running Auth Service

Navigate to

```
services/auth-service
```

Run

```powershell
mvn spring-boot:run
```

Application starts on

```
http://localhost:8081
```

---

# Swagger

```
http://localhost:8081/swagger-ui/index.html
```

---

# Health Check

```
http://localhost:8081/actuator/health
```

Expected response

```json
{
  "status":"UP"
}
```

---

# Useful Docker Commands

Start containers

```powershell
docker compose up -d
```

Stop containers

```powershell
docker compose down
```

Restart containers

```powershell
docker compose restart
```

View running containers

```powershell
docker ps
```

View all containers

```powershell
docker ps -a
```

View logs

```powershell
docker compose logs
```

Follow logs

```powershell
docker compose logs -f
```

---

# Useful PostgreSQL Commands

Connect

```powershell
psql -U postgres
```

List databases

```sql
\l
```

Connect to database

```sql
\c inridemart_auth
```

List tables

```sql
\dt
```

Describe table

```sql
\d users
```

Exit

```sql
\q
```

---

# Current Development Workflow

1. Start Docker Desktop.

2. Start infrastructure.

```
docker compose up -d
```

3. Verify containers.

```
docker ps
```

4. Run Spring Boot service.

```
mvn spring-boot:run
```

5. Verify API.

```
http://localhost:8081/actuator/health
```

6. Open Swagger.

```
http://localhost:8081/swagger-ui/index.html
```

---

# Future Services

- Auth Service ✅
- Ride Service
- Order Service
- Wallet Service
- Payment Service
- Merchant Service
- Inventory Service
- Notification Service
- API Gateway
- Config Server
- Discovery Server
- Monitoring Stack



<!-- But Docker is currently not available in your PowerShell PATH:
docker : The term 'docker' is not recognized
You need Docker Desktop installed/running, then either restart PowerShell or temporarily add Docker to PATH:
$env:Path += ";C:\Program Files\Docker\Docker\resources\bin"
Then verify:
docker --version
docker compose version
Run Infrastructure Services
From project root:
cd D:\InRideMart
docker compose up -d
This starts:
PostgreSQL on 5432
Redis on 6379
Kafka on 9092
Check containers:
docker ps
Run Auth Service
Option 1, from root:
cd D:\InRideMart
mvn -pl services/auth-service spring-boot:run
Option 2, from service folder:
cd D:\InRideMart\services\auth-service
mvn spring-boot:run
The auth service runs on:
http://localhost:8081
Verify It Is Running
Health check:
http://localhost:8081/actuator/health
Swagger UI:
http://localhost:8081/swagger-ui.html
or:
http://localhost:8081/swagger-ui/index.html
Stop Services
Stop Docker infrastructure:
cd D:\InRideMart
docker compose down -->