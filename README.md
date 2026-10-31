# GoodCycle — Drug Dispose System

GoodCycle helps customers arrange safe collection or drop-off of unused and expired medicines, then lets support staff track each return through disposal. The first release is a locally runnable MVP built from the project brief.

> **Safety and launch notice:** This platform is for collection by an authorized pharmacy or waste-disposal partner. It must not be used to sell, share, or redistribute medicines. This MVP does not verify local service availability, connect to a disposal partner, or confirm that an item was physically destroyed. Complete the production checklist below before accepting real customer or medicine data.

## What’s implemented

- **React frontend:** customer request flow, tracking, disposal guidance, and support/admin workspaces.
- **Identity service (Spring Boot / Java 25):** phone OTP registration/sign-in, short-lived JWT access tokens, customer records, OTP expiry and attempt limits, and a 60-second resend cooldown.
- **Disposal service (Spring Boot / Java 25):** persisted requests, customer-only request listing, staff queue/status management, and Kafka lifecycle events.
- **MySQL 8.4:** local persistence for customer and disposal records.
- **Kafka 4.1:** `disposal.events` lifecycle events.
- **Docker Compose:** runs the frontend, both services, MySQL, and Kafka together.

The local profile intentionally exposes the OTP in the browser response and provides demo-only staff role switching. Neither feature is enabled as a production authentication method.

## Run the full app

Requirements: Docker Desktop with Compose v2.

From the repository root:

```powershell
docker compose up --build -d
docker compose ps
```

Open **http://localhost:8080**. Initial builds download the Java, Node, MySQL, Kafka, and Nginx images and may take a few minutes.

In the app, select **Customer**, choose **New return**, enter an E.164 phone number (for example `+919876543210`), and use the demo OTP shown on screen. Submit a request. Switch to **Support** or **Admin** to review the queue and update a request status. Role switching is local-demo-only; production staff accounts must be provisioned by a secure process.

The API is also exposed locally at `http://localhost:8081` (identity) and `http://localhost:8082` (disposal). The frontend proxies API calls through port 8080.

To stop containers while retaining database and Kafka data:

```powershell
docker compose down
```

To also delete the local database and Kafka volumes (this permanently removes local demo data):

```powershell
docker compose down -v
```

## Local frontend development

Requirements: Node.js 20.19+ or 22.12+ and npm. Start the backend dependencies first with `docker compose up -d mysql kafka identity-service disposal-service`, then run:

```powershell
cd frontend
npm ci
npm run dev
```

Open the Vite URL (normally http://localhost:5173). Vite proxies `/api/auth` and `/api/disposals` to the local Spring Boot services.

Build the static frontend bundle:

```powershell
cd frontend
npm run build
```

## API outline

| Service | Endpoint | Purpose |
|---|---|---|
| Identity | `POST /api/auth/otp/request` | Request phone verification |
| Identity | `POST /api/auth/otp/verify` | Verify OTP and receive a JWT |
| Identity (local profile only) | `POST /api/auth/local-token` | Obtain a demo customer/support/admin token |
| Disposal | `GET /api/disposals` | List a customer’s own requests or the staff queue |
| Disposal | `POST /api/disposals` | Create a request as the authenticated customer |
| Disposal | `PATCH /api/disposals/{id}/status` | Update a request as support/admin |

Phone numbers must use E.164 format. The disposal API derives the request owner from the verified JWT rather than trusting a phone number supplied in the request body.

## Production readiness work still required

This codebase is a runnable MVP, **not a go-live-ready regulated service**:

- Connect and contract with authorized pharmacy/waste handlers; verify coverage, accepted items, custody, and lawful disposal records for each operating region.
- Use the production Spring profile and provision a verified Twilio sender (`TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_FROM_NUMBER`). The local profile returns the OTP to the client and is not suitable for deployment.
- Replace all local Compose credentials and the sample `JWT_SECRET` with managed secrets and a high-entropy secret. Use TLS, restricted network access, production-grade key rotation, and secure administrative provisioning.
- Replace the in-memory OTP challenge store with a shared, rate-limited store (for example Redis) before running multiple identity-service replicas. Apply additional abuse controls and monitoring.
- Use schema migrations instead of `ddl-auto: update`; configure database backups, retention, access auditing, and privacy/data deletion workflows.
- Use a transactional outbox or equivalent reliable delivery for database/Kafka events; configure Kafka authentication, encryption, retention, and monitoring.
- Add operational logging/metrics, alerts, support escalation, tested recovery procedures, privacy notices, consent, and legal/regulatory review.
- Add Kubernetes manifests, production deployment automation, and AWS infrastructure only after the target regions and operating model are established.

The app avoids collecting detailed medicine or health information; keep return notes general. Never put secrets in source control or enter real customer data into the local demo.
