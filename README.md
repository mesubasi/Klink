<div align="center">

# ⚡ Klink — Next-Gen URL Shortener & Telemetry Engine

**Modern, ultra-performant, and enterprise-grade URL shortening & link analytics platform.**  
Built with **Spring Boot 3**, **Redis In-Memory Cache**, **RabbitMQ Message Broker**, **PostgreSQL / H2**, and **Next.js 16 (App Router + Tailwind CSS v4)**.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-16.3.1-black?logo=next.js)](https://nextjs.org/)
[![Redis](https://img.shields.io/badge/Redis-Cache%20%3C2ms-red?logo=redis)](https://redis.io/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Async%20Pipeline-orange?logo=rabbitmq)](https://www.rabbitmq.com/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

</div>

---

## 🌟 Key Highlights & Features

- **⚡ Sub-2ms Redirections**: In-memory Redis caching bypasses database queries on hot paths for instantaneous HTTP 302 redirects.
- **🛡️ Multi-layered Security**:
  - Strict URL sanitization preventing CRLF injection and dangerous pseudo-protocols (`javascript:`, `data:`, `vbscript:`).
  - Anti-Phishing and Malware detection via Google Safe Browsing and VirusTotal scanners.
  - Role-based Access Control (RBAC) & Spring Security with JWT and BCrypt hashing.
  - Two-Factor Authentication (2FA) with TOTP QR codes (Google Authenticator / 1Password).
  - Rate limiting (Redis Token Bucket / Sliding Window) on all endpoints.
- **📊 Real-time Click Telemetry & Analytics**:
  - Asynchronous, loss-free event streaming via **RabbitMQ** to avoid blocking redirect latency.
  - Granular device, browser, referrer, country, and city analytics.
  - Smart **Bot / Crawler Detection** (Googlebot, Bing, Twitterbot, Curl, etc.) to separate bot traffic from human clicks.
- **🛡️ Security Preview Shield**:
  - Optional intermediate warning screen verifying domain SSL/TLS certificate and safety scores before redirecting.
- **🔒 Password-Protected Links & Geo/IP Restrictions**:
  - Secure links with BCrypt passwords or restrict access by Country code / CIDR subnet with fallback redirection URLs.
- **🎨 Dub.co / Modern SaaS UI**:
  - High-contrast Zinc theme with micro-interactions, dark telemetry cards, and responsive tables.
  - Instant clipboard paste, QR code export, batch URL processor, and CSV/PDF reports.

---

## 🏗️ Architecture Flow

```
Visitor Click (HTTP GET /xyz)
       │
       ├───► [Redis Cache] ──(Hit <2ms)──► HTTP 302 Redirect to Target URL
       │            │
       │          (Miss)
       │            ▼
       │      [Database Query] ──► Cache to Redis ──► HTTP 302 Redirect
       │
       └───► [RabbitMQ Event Broker] (url.click.queue)
                    │
                    ▼
       [ClickEventConsumer] (Background Async Worker)
                    │
                    ├── Geo-IP Resolution (Country / City)
                    ├── Bot / Crawler Detection
                    └── Persist to ClickAnalytics Repository
```

---

## 🚀 Quick Start (Development)

### 1. Prerequisites
- **Java 17+** (JDK)
- **Node.js 18+** & **npm**
- **Docker** & **Docker Compose** (for Redis & RabbitMQ)

### 2. Start Infrastructure (Redis & RabbitMQ)
```bash
docker-compose up -d
```

### 3. Start Backend (Spring Boot)
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```
The REST API and Swagger UI will be running at:
- **API Base**: `http://localhost:8080`
- **Swagger Documentation**: `http://localhost:8080/swagger-ui.html`
- **H2 Database Console**: `http://localhost:8080/h2-console`

### 4. Start Frontend (Next.js)
```bash
cd frontend
npm install
npm run dev
```
Open [http://localhost:3000](http://localhost:3000) in your browser.

---

## ⚙️ Environment Variables Configuration

Copy `.env.example` to `.env` to configure your environment:

| Variable | Default | Description |
| :--- | :--- | :--- |
| `PORT` | `8080` | Spring Boot server port |
| `APP_DOMAIN` | `http://localhost:8080` | Public domain for generated short links |
| `JWT_SECRET` | *(Base64 Key)* | 256-bit secret key for JWT signing |
| `JWT_EXPIRATION_MS` | `86400000` | JWT expiration in milliseconds (24h) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,...` | Allowed CORS origins (comma-separated) |
| `RATE_LIMIT_PER_MINUTE`| `60` | Max requests per minute per IP |
| `REDIS_HOST` | `localhost` | Redis server host |
| `REDIS_PORT` | `6379` | Redis server port |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ broker host |
| `RABBITMQ_PORT` | `5672` | RabbitMQ AMQP port |
| `GOOGLE_SAFE_BROWSING_API_KEY` | *(Optional)* | Google Safe Browsing API v4 key |
| `VIRUSTOTAL_API_KEY` | *(Optional)* | VirusTotal API v3 key |

---

## 🔒 Security Best Practices Implemented

- **CRLF & Header Injection Immune**: Strips carriage returns and line feeds to strictly prevent HTTP Response Splitting.
- **Protocol Whitelisting**: Strict `http://` / `https://` validation; rejects `javascript:`, `data:`, `file:`, `vbscript:`, etc.
- **Zero Raw 500 Error Leakage**: Exception handler shields internal stack traces and database schemas from client responses.
- **Admin Endpoint Authorization**: Strictly enforces `ROLE_ADMIN` on all telemetry and system management routes (`/api/v1/admin/**`).
- **Cryptographic Password Storage**: Utilizes BCrypt with salt rounds for user credentials and link passwords.

---

## 📄 License

This project is licensed under the [Apache 2.0 License](LICENSE).
