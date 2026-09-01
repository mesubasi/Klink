<div align="center">

# ⚡ Klink — Enterprise URL Shortener & Link Management Engine

**Modern, ultra-performant, and feature-complete link management, analytics & bio-page platform.**  
Built with **Spring Boot 3 (Java 17/21)**, **Redis In-Memory Cache**, **RabbitMQ Event Streaming**, **PostgreSQL / H2**, and **Next.js 16 (React 19 + Tailwind CSS v4)**.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-16.3.1-black?logo=next.js)](https://nextjs.org/)
[![Docker](https://img.shields.io/badge/Docker-One--Click%20Compose-2496ED?logo=docker)](docker-compose.yml)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-Sub--2ms%20Cache-red?logo=redis)](https://redis.io/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Async%20Telemetry-orange?logo=rabbitmq)](https://www.rabbitmq.com/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

</div>

---

## 🌟 Key Capabilities & Feature Matrix

### 🚀 1. Intelligent URL Shortening & Routing
- **Sub-2ms Redirections**: High-speed in-memory Redis caching avoids database trips on hot paths for instantaneous HTTP 302 redirects.
- **Base62 & Custom Aliases**: Generate clean 7-character short codes or customize your own branded vanity links.
- **📱 Device-Based Deep Linking**: Route users to specialized URLs based on their client device (**iOS**, **Android**, **Desktop**).
- **🌍 Geo-Blocking & Fallback Routing**: Restrict access by ISO country codes or IP CIDR subnets with custom fallback destinations.
- **🔒 Password-Protected Links & Security Preview Shield**: Encrypt links with BCrypt-hashed passwords or display a security preview screen with SSL/TLS domain inspection.

### 🎨 2. Dynamic QR Code Studio
- **Custom Color Palettes & Presets**: 6 built-in themes (Classic, Cyber Neon, Emerald, Ocean, Sunset, Luxury Gold) plus full Hex/RGB pickers.
- **Stylized Dot & Eye Patterns**: Choose between **Square**, **Circular Dots**, and **Smooth Rounded** modules.
- **🖼️ Brand Logo & Icon Badging**: Embed social/web icons or upload custom brand logos with white protective badge padding.
- **Error Correction Level H (%30)**: Guarantees 100% instant scannability even with center logo overlays.
- **📐 Multi-Format Export**: Download in **Standard Web PNG (512px)**, **Ultra HD 4K (2048px)**, **Lossless Vector SVG**, or copy directly to clipboard.
- **⚡ Live Contrast & Scannability Gauge**: Real-time scannability health check indicator to prevent low-contrast print errors.

### 🏥 3. Broken Link Health Monitor (Automated Cron)
- **15-Minute Background Cron Scanner**: Asynchronously verifies target server availability across all active links.
- **HTTP HEAD & GET Fallback Engine**: Lightweight connection checks with SSL verification, DNS resolution, and latency (`ms`) tracking.
- **Status Dashboard**: Instant visibility into `HEALTHY`, `DEGRADED`, or `BROKEN` links with HTTP status code badges and error diagnostics.

### 🌳 4. Link-in-Bio Platform (`/bio/{username}`)
- **Personalized Creator Profiles**: Create beautiful mobile-first bio pages to showcase all your links in one place.
- **Custom Themes & Social Icons**: Modern color schemes, verified badges, and social media connectivity.
- **Bio Telemetry**: Real-time tracking of profile pageviews and individual link click-through rates.

### 📊 5. Real-Time Telemetry & Advanced Analytics
- **Lossless Async Streaming via RabbitMQ**: Decouples click logging from redirection latency to preserve peak throughput.
- **Granular Metrics**: Geo-location (Country / City via MaxMind GeoIP), referrers, device types, browsers, and operating systems.
- **🤖 Bot & Crawler Detection Engine**: Automatically identifies search engine spiders and automated scrapers (Googlebot, Bingbot, Twitterbot, Curl, etc.) to keep human analytics clean.
- **Export Reports**: Instant CSV and formatted PDF summary downloads.

### 🛡️ 6. Enterprise-Grade Security Hardening
- **SSRF Prevention**: Blocks internal network scanning (`127.0.0.1`, `localhost`, `10.0.0.0/8`, `192.168.0.0/16`, `169.254.169.254` AWS IMDS).
- **SVG XSS Sanitization**: Validates hex color inputs against strict regex rules before SVG XML rendering.
- **Password Brute-Force Throttling**: Temporary 5-minute IP lockout after 5 consecutive incorrect link password attempts.
- **Two-Factor Authentication (2FA / TOTP)**: RFC 6238-compliant authenticator support (Google Authenticator, 1Password, Authy).
- **Cryptographic API Keys**: SHA-256 hashed API keys with per-key rate limits and prefix visibility.
- **Modern Security Headers**: Enforces `X-Content-Type-Options: nosniff`, `Referrer-Policy: strict-origin-when-cross-origin`, and `X-Frame-Options: SAMEORIGIN`.

---

## 🏗️ System Architecture

```
                                  ┌────────────────────────┐
                                  │      Client / User     │
                                  └───────────┬────────────┘
                                              │
                      ┌───────────────────────┴───────────────────────┐
                      ▼                                               ▼
           [Next.js 16 Web Dashboard]                      [HTTP GET /{shortCode}]
             (Port 3000 / React 19)                                   │
                      │                                               ▼
                      │                                    ┌────────────────────┐
                      │                                    │  Rate Limiter &    │
                      │                                    │ Security Intercept │
                      │                                    └──────────┬─────────┘
                      ▼                                               │
        ┌───────────────────────────┐                                 ▼
        │   Spring Boot 3 REST API  │                      ┌────────────────────┐
        │   (Port 8080 / Java 17)   │                      │  Redis Cache (Hit) │────(Sub-2ms)──► HTTP 302 Target URL
        └─────────────┬─────────────┘                      └──────────┬─────────┘
                      │                                               │ (Miss)
        ┌─────────────┼─────────────┐                                 ▼
        ▼             ▼             ▼                      ┌────────────────────┐
  [PostgreSQL 16]  [Redis 7]  [RabbitMQ 3] ◄──(Async Event)│ Database Lookup &  │────► HTTP 302 Target URL
  (Persistent DB)  (Cache &   (Event Queue)                │ Cache Invalidation │
                   RateLimit)       │                      └────────────────────┘
                                    ▼
                         [ClickEventConsumer]
                         ├── MaxMind Geo-IP Resolution
                         ├── Bot & Crawler Classifier
                         └── Persist to ClickAnalytics
```

---

## ⚡ One-Click Quickstart (Docker Compose)

The fastest and easiest way to run the entire Klink stack (**PostgreSQL + Redis + RabbitMQ + Spring Boot Backend + Next.js Frontend**) is via Docker Compose:

```bash
# 1. Clone the repository
git clone https://github.com/mesubasi/Klink.git
cd Klink

# 2. Launch the entire stack in one click
docker compose up -d --build
```

That's it! Once containers are healthy:
- 🌐 **Web Dashboard**: [http://localhost:3000](http://localhost:3000)
- 🔌 **REST API & Endpoints**: [http://localhost:8080](http://localhost:8080)
- 📖 **Interactive Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- 🐰 **RabbitMQ Management**: [http://localhost:15672](http://localhost:15672) (User/Pass: `klink_mq` / `klink_mq_pass`)

To stop all services:
```bash
docker compose down
```

---

## 💻 Manual Local Development Setup

If you want to run backend and frontend natively for development:

### 1. Prerequisites
- **Java 17+ / 21** & **Maven 3.9+**
- **Node.js 20+** & **npm**
- **Docker** (to run Redis & RabbitMQ locally)

### 2. Start Supporting Infrastructure
```bash
docker compose up -d redis rabbitmq postgres
```

### 3. Start Backend (Spring Boot)
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```
*Note: In `dev` profile, backend automatically initializes in-memory H2 database (`http://localhost:8080/h2-console`) with seeded test accounts (`admin` / `admin123` and `user` / `password`).*

### 4. Start Frontend (Next.js)
```bash
cd frontend
npm install
npm run dev
```
Open [http://localhost:3000](http://localhost:3000) in your browser.

---

## ⚙️ Environment Variables Reference

Create a `.env` file based on `.env.example`:

| Environment Variable | Default (Docker) | Description |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `prod` | Active Spring profile (`dev` or `prod`) |
| `DB_NAME` | `klinkdb` | PostgreSQL database name |
| `DATABASE_USERNAME` | `klink_admin` | Database username |
| `DATABASE_PASSWORD` | `klink_secret_password` | Database password |
| `REDIS_HOST` | `redis` | Redis server hostname |
| `REDIS_PORT` | `6379` | Redis server port |
| `RABBITMQ_HOST` | `rabbitmq` | RabbitMQ broker hostname |
| `RABBITMQ_PORT` | `5672` | RabbitMQ AMQP port |
| `JWT_SECRET` | *(Base64 String)* | Minimum 256-bit cryptographic key for JWT signing |
| `APP_DOMAIN` | `http://localhost:8080` | Base public URL for short code routing |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,...` | Allowed CORS origins for the frontend client |
| `GOOGLE_SAFE_BROWSING_API_KEY` | *(Optional)* | Google Safe Browsing API v4 key for malware checks |
| `VIRUSTOTAL_API_KEY` | *(Optional)* | VirusTotal API v3 key for threat intelligence |
| `NEXT_PUBLIC_API_URL` | `http://localhost:8080/api/v1` | Backend API base URL consumed by Next.js |

---

## 🔌 API Documentation & Key Endpoints

| Method | Endpoint | Description | Auth |
| :--- | :--- | :--- | :--- |
| `GET` | `/{shortCode}` | Fast HTTP 302 Redirection (Cached) | Public |
| `POST` | `/api/v1/urls/shorten` | Shorten a single URL with deep-link & security options | Public / User |
| `POST` | `/api/v1/urls/bulk-shorten` | Batch shorten multiple URLs (up to 50) | User / Admin |
| `GET` | `/api/v1/urls/my-urls` | Retrieve all shortened URLs for authenticated user | User / Admin |
| `GET` | `/api/v1/urls/{shortCode}/analytics` | Comprehensive click telemetry & geo stats | Owner / Admin |
| `GET` | `/api/v1/urls/{shortCode}/qrcode` | Generate dynamic customized PNG or SVG QR code | Public |
| `POST` | `/api/v1/urls/qrcode/custom` | Generate standalone custom QR code from any payload | Public |
| `POST` | `/api/v1/urls/{shortCode}/health-check` | Trigger on-demand HTTP health check for target link | Owner / Admin |
| `GET` | `/api/v1/bio/{username}` | Fetch public creator bio page & links | Public |
| `POST` | `/api/v1/bio/me` | Create or update authenticated user's bio page | User / Admin |
| `POST` | `/api/v1/api-keys/apply` | Apply for a developer API key | User / Admin |
| `POST` | `/api/v1/auth/2fa/setup` | Initialize TOTP 2FA secret and QR code | User / Admin |

Full interactive API explorer is available at: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 🧪 Testing & Code Quality

Klink includes a comprehensive test suite covering unit, integration, and security test cases:

```bash
# Run full Maven test suite
mvn test

# Run frontend build check
npm --prefix frontend run build
```

---

## 📄 License

This project is licensed under the [Apache 2.0 License](LICENSE).
