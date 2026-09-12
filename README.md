# CasaCrew — Backend API

REST API voor CasaCrew, een multi-tenant SaaS voor het beheer van verhuurpanden (bewoners, facturen, schoonmaak, documenten en meer).
Gebouwd met **Spring Boot 3** en **Java 21**.

De volledige API-documentatie (alle endpoints, request- en responsemodellen) wordt automatisch gegenereerd via **springdoc-openapi** en is bij het lokaal draaien beschikbaar op `http://localhost:8080/swagger-ui.html` (raw OpenAPI-schema op `/v3/api-docs`). In productie staat dit uit (zie `application-prod.yml`).

---

## Technische stack

| Onderdeel          | Keuze                                          |
|--------------------|-------------------------------------------------|
| Framework          | Spring Boot 3.3.4                                |
| Taal               | Java 21                                          |
| Database           | PostgreSQL, migraties via Flyway                 |
| Multi-tenancy      | Expliciete `organization_id` op elke entiteit    |
| Authenticatie      | Opaak, database-backed sessietoken (Bearer) + Google OAuth |
| Wachtwoord hashing | BCrypt (sterkte 8)                               |
| Betalen            | bunq.me betaallinks                              |
| Mail               | SMTP via JavaMailSender                          |
| WhatsApp           | Twilio                                           |
| Tests              | JUnit 5, Mockito, Testcontainers                 |
| Coverage           | JaCoCo                                           |
| Build              | Maven                                            |

---

## Rollen

| Rol       | Omschrijving                                                    |
|-----------|-------------------------------------------------------------------|
| `ADMIN`   | Beheert gebruikers, kamers, facturen, documenten en taken         |
| `STUDENT` | Ziet eigen facturen, betalingen, taken en documenten               |
| `CLEANER` | Beheert schoonmaaktaken en rapporteert incidenten                  |

Alle endpoints behalve `/api/auth/**`, `/api/organizations` (self-registratie) en `/actuator/health` vereisen een geldig **Bearer-sessietoken** in de `Authorization`-header, uitgegeven door `POST /api/auth/login` of `/api/auth/google-login`.

---

## Lokaal draaien

### Vereisten

- Java 21
- Maven 3.9+
- PostgreSQL 15+ (of Docker)
- Docker (alleen voor integratietests via Testcontainers)

### 1. Database aanmaken

```sql
CREATE DATABASE casacrew;
```

Flyway maakt bij het opstarten automatisch alle tabellen aan (`src/main/resources/db/migration`) — geen handmatige schema-setup nodig.

### 2. `.env` aanmaken

Maak een `.env` bestand in de projectroot. Zie `.env.example` voor alle variabelen:

```env
SPRING_PROFILES_ACTIVE=dev

DB_URL=jdbc:postgresql://localhost:5432/casacrew
DB_USERNAME=<jouw-db-gebruiker>
DB_PASSWORD=<jouw-db-wachtwoord>

SESSION_EXPIRY_SECONDS=3600
GOOGLE_CLIENT_ID=<google-oauth-client-id>

APP_CORS_ALLOWED_ORIGINS=http://localhost:5173
APP_UPLOAD_DIR=uploads
FRONTEND_URL=http://localhost:5173
RENT_AMOUNT=350.00

SEED_ENABLED=false
SEED_ADMIN_EMAIL=<admin-e-mailadres>
SEED_ADMIN_PASSWORD=<admin-wachtwoord>
SEED_CLEANER_EMAIL=<cleaner-e-mailadres>
SEED_CLEANER_PASSWORD=<cleaner-wachtwoord>
SEED_STUDENT_EMAILS=<email1>,<email2>
SEED_STUDENT_PASSWORD=<student-wachtwoord>

MAIL_ENABLED=false
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<smtp-adres>
MAIL_PASSWORD=<smtp-wachtwoord>
MAIL_FROM=<afzenderadres>
MAIL_BCC_ADMIN=<bcc-adres>

TWILIO_ACCOUNT_SID=<twilio-account-sid>
TWILIO_AUTH_TOKEN=<twilio-auth-token>
TWILIO_WHATSAPP_FROM=<twilio-whatsapp-nummer>

VAPID_PUBLIC_KEY=<vapid-public-key>
VAPID_PRIVATE_KEY=<vapid-private-key>
VAPID_SUBJECT=mailto:admin@casacrew.nl
FIREBASE_SERVICE_ACCOUNT_JSON=<inhoud-van-firebase-service-account.json-als-één-regel>
```

Bunq.me-gebruikersnaam, IBAN en rekeninghouder zijn per-organisatie instellingen (niet langer een env var) en worden door een admin ingesteld via `GET`/`PUT /api/admin/organization/payment-settings`. Admin-WhatsApp-notificaties gaan naar de telefoonnummers van de ADMIN-gebruikers van de betreffende organisatie, niet naar een vaste lijst.

**Pushmeldingen**: `VAPID_PUBLIC_KEY`/`VAPID_PRIVATE_KEY` zijn nodig voor Web Push (browser/PWA) en moeten eenmalig gegenereerd worden (een geldig paar staat al klaar voor lokale ontwikkeling in `.env`; genereer een eigen paar voor productie, bv. met `npx web-push generate-vapid-keys`). `FIREBASE_SERVICE_ACCOUNT_JSON` is optioneel en alleen nodig voor FCM (native app-pushmeldingen) — zonder deze variabele blijft FCM een no-op (gelogd bij opstarten), Web Push blijft gewoon werken.

> Het `.env` bestand staat in `.gitignore`. Zet nooit wachtwoorden of sleutels in versiebeheer.

### 3. Opstarten

```bash
mvn spring-boot:run
```

De API is bereikbaar op `http://localhost:8080`.

### 4. Organisatie aanmaken

Nieuwe klanten registreren zichzelf via `POST /api/organizations` — dit maakt in één transactie de organisatie én de eerste admin-gebruiker aan, en geeft direct een sessietoken terug. Er is geen los seed-mechanisme nodig voor productiegebruik.

---

## Tests uitvoeren

### Unit tests (geen Docker nodig)

```bash
mvn test
```

### Integratietests (Docker vereist)

De integratietests gebruiken **Testcontainers**: er wordt automatisch een tijdelijke PostgreSQL Docker-container gestart en na afloop opgeruimd. Er wordt geen `@MockBean` gebruikt — alle lagen worden echt aangestuurd.

**Vereiste:** Docker Desktop moet draaien voordat je deze tests uitvoert.

```bash
# Start Docker Desktop, daarna:
mvn verify
```

Of via het Maven-profiel:

```bash
mvn verify -PrunIT
```

Controleer of Docker actief is met:

```bash
docker info
```

### Codecoverage

```bash
mvn verify
open target/site/jacoco/index.html
```

Gedekte services met unit tests: `InvoiceService`, `MailService`, `CleaningTaskService`, `CleaningScheduleService`, `RoomService`, `PaymentService`.

---

## Projectstructuur

```
src/
├── main/java/com/casacrew/
│   ├── config/       # SecurityConfig, GlobalExceptionHandler, seeders
│   ├── controller/   # REST-controllers
│   ├── dto/          # Request- en response-DTOs (met Bean Validation)
│   ├── jobs/         # Geplande taken (@Scheduled)
│   ├── model/        # JPA-entiteiten (elk met organization_id)
│   ├── repository/   # Spring Data JPA repositories
│   ├── security/     # SessionAuthenticationFilter, AuthSessionService
│   └── service/      # Bedrijfslogica
└── test/java/com/casacrew/
    ├── integration/  # Integratietests (Testcontainers + MockMvc)
    └── service/      # Unit tests (Mockito)
```

---

## Deployment

`render.yaml` beschrijft een Render Blueprint (Docker web service + losse Postgres-database) — via **New + → Blueprint** in het Render-dashboard, repo selecteren, klaar. Zie het bestand zelf voor de exacte env-var-koppeling.

---

## Beveiliging

- **Sessietoken** — opaak, database-backed, server-side intrekbaar; elk verzoek valideert het token in `SessionAuthenticationFilter` (geen JWT, geen client-side decodeerbare payload)
- **Multi-tenancy** — elke entiteit heeft een verplichte `organization_id`; alle queries zijn org-scoped op service-niveau, niet alleen op rolniveau
- **Ownership-check** — studenten kunnen uitsluitend hun eigen facturen, PDF's en betalingen opvragen; dit wordt gecontroleerd in de service-laag
- **Invoervalidatie** — Bean Validation (`@Valid`, `@NotBlank`, `@Email`, `@Size` e.d.) op alle request-DTOs; de `GlobalExceptionHandler` mapt validatiefouten naar HTTP 400
- **BCrypt** — wachtwoorden worden nooit als plain-text opgeslagen
- **CORS** — geconfigureerd via omgevingsvariabelen; geen wildcard in productie
- **Uploads** — validatie op bestandstype en padtraversal; maximale bestandsgrootte 5 MB
- **Geheimen** — alle sleutels en wachtwoorden via `.env` / omgevingsvariabelen; nooit hardcoded in de codebase
