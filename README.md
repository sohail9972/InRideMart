# InRideMart

Enterprise-grade AI-powered in-ride commerce platform.

This repository is organized as a modular monorepo. The first production-ready slice is the `auth-service`; the remaining bounded contexts are documented and scaffolded so they can be implemented one module at a time with the same architecture.

## Current Implementation Status

| Module | Status |
| --- | --- |
| Authentication | Implemented |
| Customer | Implemented |
| Driver | Planned |
| Merchant | Planned |
| Ride Booking | Planned |
| Product Catalog | Implemented |
| Shopping Cart | Implemented |
| Orders | Implemented |
| Inventory | Planned |
| AI Recommendation | Planned |
| Payments | Planned |
| Notifications | Planned |
| Analytics | Planned |
| Admin Dashboard | Planned |
| Fraud Detection | Planned |

## Quick Start

The project currently includes `auth-service`, `customer-service`, and `catalog-service`. A Next.js passenger catalog frontend is available under `frontend`.

### Prerequisites

- Java 21 or later
- Maven
- Docker Desktop
- Docker Compose
- Node.js 20.9 or later (for `frontend`)

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

- PostgreSQL on `5433` (Docker uses this port to avoid a local PostgreSQL conflict)
- Redis on `6379`
- Kafka on `9092`

Check that the containers are running:

```powershell
docker ps
```

### 2. Run Auth Service

Set the local runtime secrets in the same PowerShell session. The values in `.env.example` are a template only; do not commit a real `.env` file.

```powershell
$env:INRIDEMART_DB_PASSWORD = "InRide@123"
$env:JWT_SECRET = "replace-this-with-a-local-32-byte-minimum-secret"
```

Both services must use the same `JWT_SECRET`, because `customer-service` validates access tokens issued by `auth-service`.

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

### 3. Run Customer Service

The customer service uses a separate database named `inridemart_customer`. Fresh Docker volumes create this database automatically from `deploy/postgres/init`.

If your PostgreSQL Docker volume already existed before this service was added, create the database manually once:

```powershell
docker exec -it inridemart-postgres psql -U inridemart -d inridemart_auth
```

Then run:

```sql
CREATE DATABASE inridemart_customer OWNER inridemart;
GRANT ALL PRIVILEGES ON DATABASE inridemart_customer TO inridemart;
\q
```

Run the Spring Boot service from the project root:

```powershell
cd D:\InRideMart
mvn -pl services/customer-service spring-boot:run
```

The application starts on:

```text
http://localhost:8082
```

### 4. Verify the Applications

Auth health check:

```text
http://localhost:8081/actuator/health
```

Auth Swagger UI:

```text
http://localhost:8081/swagger-ui.html
```

Customer health check:

```text
http://localhost:8082/actuator/health
```

Customer Swagger UI:

```text
http://localhost:8082/swagger-ui.html
```

### 5. Run Catalog Service

The catalog service uses the `inridemart_catalog` database. Fresh Docker volumes create it automatically from `deploy/postgres/init`.

For an existing PostgreSQL Docker volume, create it once:

```powershell
docker exec -it inridemart-postgres psql -U inridemart -d inridemart_auth -c "CREATE DATABASE inridemart_catalog OWNER inridemart;"
```

Start the service in a terminal that has `INRIDEMART_DB_PASSWORD` set:

```powershell
cd D:\InRideMart
mvn -pl services/catalog-service spring-boot:run
```

Catalog endpoints are public for passenger discovery in this MVP:

```text
http://localhost:8083/actuator/health
http://localhost:8083/swagger-ui.html
http://localhost:8083/api/v1/catalog/categories
http://localhost:8083/api/v1/catalog/products?query=charger&maxPrice=1000
```

### 6. Run Passenger Web

In another terminal, install frontend dependencies once and start the Next.js development server:

```powershell
cd D:\InRideMart\frontend
npm install
npm run dev
```

Open `http://localhost:3000`. The frontend reads `CATALOG_API_BASE_URL` from `frontend/.env.local` when an override is needed; see `frontend/.env.example`.

### 7. Stop Services

Stop each Spring Boot service with `Ctrl + C` in the terminal where it is running.

Stop Docker infrastructure:

```powershell
cd D:\InRideMart
docker compose down
```

Note: run the Spring Boot services with Maven on your machine for local development. The current `application.yml` files point to `localhost` infrastructure ports exposed by `docker-compose.yml`.
Database passwords, JWT signing keys, and infrastructure endpoints are read from environment variables at runtime. See `.env.example` for the supported local overrides.

## Architecture

Read the platform design package in [docs/system-design.md](docs/system-design.md).

## Hackathon Demo Architecture

```text
Next.js passenger web -> Auth / Customer / Catalog / Cart / Order / Payment / AI
                                                     |                 |
                                                  PostgreSQL       OpenAI Responses API
AI -> Catalog HTTP API (allow-listed products only) -> recommendations -> Cart
Cart -> Order checkout -> Payment -> Order CONFIRMED -> HANDED_OVER -> COMPLETED
```

| Service | Port | Owns |
| --- | --- | --- |
| Auth | 8081 | Registration, login, JWT issuance |
| Customer | 8082 | Passenger profiles |
| Catalog | 8083 | Available in-ride products |
| Cart | 8084 | Passenger carts |
| Order | 8085 | Checkout and in-ride status tracking |
| AI | 8086 | Catalog-grounded shopping recommendations |
| Payment | 8087 | Mock payment records and confirmation |

All services validate the Auth-issued JWT using `JWT_SECRET`; each owns its own PostgreSQL database. AI never reads Catalog data directly from a database: it calls the Catalog HTTP API and only returns products supplied by that API.

### AI Setup

Set `OPENAI_API_KEY` and a configurable `OPENAI_MODEL` to enable Responses API reasoning. With no API key, AI Service uses deterministic, catalog-grounded recommendations so the demo remains runnable locally.

```powershell
$env:INRIDEMART_DB_PASSWORD = "InRide@123"
$env:JWT_SECRET = "your-local-32-byte-minimum-secret"
$env:OPENAI_API_KEY = ""
$env:OPENAI_MODEL = ""
docker compose up -d
```

Start each service with `mvn -pl services/<service-name> spring-boot:run`, then run `npm run dev` from `frontend`. Swagger is available at `http://localhost:<port>/swagger-ui.html`; the AI passenger experience is at `http://localhost:3000/ai`.

### Demo Walkthrough

1. Register and log in through Auth Service, then create a Customer profile.
2. Browse the public Catalog and ask AI Shopping for an in-ride need such as “I forgot my charger and have 800 INR”.
3. Add an AI recommendation to Cart and call checkout with an `Idempotency-Key`.
4. Process the mock payment with `POST /api/v1/payments`; the Order becomes `CONFIRMED`.
5. Advance the in-ride order with `PATCH /api/v1/orders/{id}/tracking` to `HANDED_OVER`, then `COMPLETED`.

### Frontend

The Next.js passenger app lives in `frontend` and is designed as a mobile-first client over the existing HTTP APIs.

| Route | Experience |
| --- | --- |
| `/` | Home, featured live Catalog products, category filters, Cart, Orders, tracking timeline, and profile login |
| `/ai` | Authenticated AI Shopping chat, conversation history, recommendation cards, suggested journey prompts, and direct Cart adds |
| `/api/catalog/*` | Catalog read proxy used by server-rendered and client Catalog views |
| `/api/proxy/{service}/*` | Same-origin proxy for Auth, Customer, Cart, Order, AI, and Payment APIs; it forwards the caller's Authorization and idempotency headers |

Run the frontend:

```powershell
cd D:\InRideMart\frontend
npm install
npm run dev
```

Use `npm run lint` and `npm run build` before demoing. Product cards are always populated from Catalog Service; catalog expansion belongs in Catalog database migrations, never in frontend constants.



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








<!-- Workflow We will Follow
main
 │
 │
 ├──────── Story-1
 │             │
 │             └──── PR ───► main
 │
 ├──────── Story-2
 │             │
 │             └──── PR ───► main
 │
 ├──────── Story-3
 │             │
 │             └──── PR ───► main
 │
 ├──────── Story-4
 │
 └──────── Story-5

Each story should be independent.

Exactly like companies.

Step 1 Create GitHub Issues (Stories)

Open your repository

InRideMart

Go to

Issues

Click

New Issue

Example

Title

IRM-001 Setup Authentication Service

Description

As a Rider

I want authentication APIs

So that I can securely login.

Acceptance Criteria

- Register API
- Login API
- JWT Token
- Password Encryption
- Unit Tests

Click

Create Issue

Now create another.

IRM-002 Driver Registration

Another

IRM-003 Rider Registration

Another

IRM-004 OTP Verification

Another

IRM-005 Forgot Password

Eventually you'll have

Issues

IRM-001
IRM-002
IRM-003
IRM-004
IRM-005
Step 2 Create Local Branch

Go to project

cd D:\InRideMart

Initialize Git if not already done:

git init

Add your remote:

git remote add origin https://github.com/sohail9972/InRideMart.git

Fetch the remote branches:

git fetch origin

Create and switch to a story branch:

git checkout -b feature/IRM-001-authentication

Verify

git branch

Output

* feature/IRM-001-authentication
main
Step 3 Start Coding

Suppose you implement

JWT
Register API
Login API
Swagger
Tests
Step 4 Commit
git add .
git commit -m "IRM-001 Implement authentication APIs"
Step 5 Push Branch
git push origin feature/IRM-001-authentication

GitHub will automatically suggest creating a Pull Request.

Step 6 Raise Pull Request

Open GitHub

You'll see

Compare & Pull Request

Click it.

Title

IRM-001 Implement Authentication APIs

Description

Implemented

✔ Register API

✔ Login API

✔ JWT Authentication

✔ Password Encryption

✔ Swagger

✔ Unit Tests

Fixes #1

(Replace #1 with the actual issue number.)

Click

Create Pull Request
Step 7 Review

Even if you're working alone, review your own PR.

Check

API Design
Code Quality
Naming
Test Cases
Swagger
Security

This builds good engineering habits.

Step 8 Merge

Once satisfied

Merge Pull Request

Delete the feature branch on GitHub if prompted.

Step 9 Update Local Main
git checkout main
git pull origin main

Delete the local feature branch:

git branch -d feature/IRM-001-authentication
Then Start Next Story
git checkout -b feature/IRM-002-driver-registration

Implement

Driver APIs

Raise PR

Merge

Repeat.

Story Naming Convention

I recommend:

feature/IRM-001-authentication

feature/IRM-002-driver-registration

feature/IRM-003-rider-registration

feature/IRM-004-order-service

feature/IRM-005-wallet-service

feature/IRM-006-product-service

feature/IRM-007-driver-inventory

feature/IRM-008-payment-service

feature/IRM-009-ride-integration

feature/IRM-010-notification-service
Commit Convention
IRM-001 Add JWT Authentication

IRM-001 Fix Login Validation

IRM-002 Add Driver Entity

IRM-002 Implement Driver Registration

IRM-003 Add Rider APIs
PR Naming
[IRM-001] Authentication APIs

[IRM-002] Driver Registration

[IRM-003] Rider APIs
Long-Term Roadmap for InRideMart
Sprint	Stories
Sprint 1	Auth Service
Sprint 2	Rider Service
Sprint 3	Driver Service
Sprint 4	Product Catalog
Sprint 5	Inventory
Sprint 6	Order Service
Sprint 7	Wallet
Sprint 8	Payments
Sprint 9	Ride Integration
Sprint 10	Notifications
Sprint 11	Recommendation Engine
Sprint 12	Analytics Dashboard
Sprint 13	Admin Portal
Sprint 14	AI Recommendations
Sprint 15	Production Deployment -->
