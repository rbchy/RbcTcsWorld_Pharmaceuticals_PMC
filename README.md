# RbcTcsWorld_Pharmaceuticals_ProductionManagementCalculator

![Java](https://img.shields.io/badge/Java-17-orange?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-brightgreen?logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql)
![Maven](https://img.shields.io/badge/Build-Maven-blue?logo=apachemaven)
![Swagger](https://img.shields.io/badge/API_Docs-OpenAPI-85EA2D?logo=swagger)
![Tests](https://img.shields.io/badge/Tests-74_automated-success)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

**Author:** Ranajit Baran Chowdhury — QA Automation Engineer / SDET
**Email:** chyranajit@gmail.com
**GitHub:** [@rbchy](https://github.com/rbchy)

A REST-API-first batch/lot calculator for pharmaceutical packaging lines,
built around one real, uploaded Sharp Packaging Solutions work order
(S180406-1 — Imodium MSR 24ct caplets, Kenvue Brands). It covers the full
pack-hierarchy breakdown, material requirement planning, partial-production
progress, **Sealed-vs-Exhaust lot allocation**, three-shift
**Startup/Running/Finished** classification, and material reconciliation —
exposed as a documented REST API, a persisted MySQL-backed record store, a
desktop-style bilingual browser UI, and backed by a 74-test automated suite
with Allure reporting.

---

## 📋 Quick Overview

| | |
|---|---|
| **Backend** | Spring Boot 3.3, Java 17, Spring Data JPA |
| **Database** | MySQL 8.0 (production), H2 in-memory (CI/test) |
| **Frontend** | Single-page bilingual HTML/CSS/vanilla JS, served as a Spring Boot static resource — no build step |
| **API Docs** | springdoc-openapi (Swagger UI at `/swagger-ui.html`) |
| **Auth** | Demo-only (browser localStorage) — see [Known Gaps](#known-gaps--honesty-notes) |
| **Testing** | JUnit 5 (unit + MockMvc/API), Selenium 4 (E2E), Allure reporting |
| **Build** | Maven (`pom.xml`), runs as a standard Spring Boot jar |

---

## ✨ Key Features

### 🧮 Batch/Lot Calculator Engine
- Pack-hierarchy breakdown: caplets → BFUs → cartons → bundles → shippers
- Material requirement (base + standard overage) per component
- Partial-production progress tracking across shifts
- Three-shift lifecycle classification: **Startup → Running → Finished**

### 🔒 Sealed vs. Exhaust Lot Allocation
- Drains Open-Partial/Exhaust lots first (by priority) before touching a new
  Sealed lot — the real GMP material-control rule seen on the source work
  order (`Must use bulk lot# ...(Exhaust)`)
- Shortfall detection with a clear bilingual error, not a raw stack trace

### ⚖️ Material Reconciliation
- Used / rejected / return-to-store per material, flagged against a
  configurable tolerance (default ±3%)

### 💾 Persisted Batch Records (new, independent MySQL schema)
- 5 tables (`batch_header`, `material_requirement`, `production_entry`,
  `lot_balance`, `reconciliation_record`) — a from-scratch schema, not tied
  to any other project's database
- Full CRUD: create → append production → save lot allocation → save
  reconciliation → fetch full detail → delete

### 📖 Interactive API Docs
- Every endpoint documented and "try-it-out"-able at `/swagger-ui.html`
  (springdoc-openapi, no separate Postman collection needed)

### 🖥️ Desktop-Style Browser UI
- Login → Register → Dashboard → tool forms flow, RbcTcsWorld watermark,
  icon-tile navigation
- Every form: centered card layout with **Add / Edit / Update / Delete /
  Display / Search / Back** action buttons
- English/বাংলা toggle across the entire UI

### 🌐 6-Language Help Center + GMP/cGMP/EHS/OEB Glossary
- English, বাংলা, Español, Français, العربية, 中文
- A dedicated glossary explains **GMP, cGMP, EHS, and OEB exposure bands**
  (1–5), and every tool panel's help topic ties back to which of those
  concepts it touches — e.g. Lot Allocation explains why OEB class matters
  when deciding how many open bulk-material containers are in play at once

### ✅ Automated Test Suite (74 tests) + Allure Reporting
- 27 unit tests, 24 stateless-API tests, ~13 persisted-API tests, ~10
  Selenium E2E tests — positive, negative, boundary, and security cases
- Full test-case design matrix (~150 planned cases) in
  `RbcTcsWorld_Test_Cases.xlsx`
- Allure report organized by layer (`@Epic`) and severity (`@Severity`),
  matching the matrix's priority ratings

---

## 🔄 How This Compares to PPES

[**PPES — Pharmaceutical Packaging Management System**](https://github.com/rbchy/PPES-Pharmaceutical-Packaging-Management)
is this author's other pharma packaging project: a full **Java Swing desktop
+ PHP/Bootstrap web** system, sharing one MySQL database directly, covering
18 tables of masters and production data end-to-end (product/customer
masters, raw material receiving, IPC inspection, pallets, shift handover,
and more) with real salted-SHA-256 authentication and 6 pre-built Excel
reports. Both projects share DNA — PPES even has its own
`batch_yield_reconciliation_calculator.html` and the same bilingual
GMP/cGMP/OEB1–5 glossary approach — but they solve different problems:

| | **PPES** | **This project (RbcTcsWorld)** |
|---|---|---|
| **Scope** | Whole packaging-line ERP: 18 tables, order intake through dispatch & waste | One focused capability: batch/lot math + Sealed/Exhaust allocation + reconciliation |
| **Architecture** | Two separate apps (Swing + PHP) talking to MySQL **directly** | One Spring Boot app; browser UI talks to a **REST API**, not the DB directly |
| **API layer** | None — each app embeds its own DB access (JDBC / PDO) | Full REST API, documented with Swagger/OpenAPI, usable by any client |
| **Frontend** | Java Swing desktop GUI *and* a separate PHP/Bootstrap web app | One bilingual (6-language Help Center) single-page browser UI |
| **Auth** | Real: salted SHA-256, shared `users` table, works identically in both apps | **Demo only** (localStorage) — a known, documented gap here |
| **Data layer** | 18-table metadata-driven generic CRUD engine (`TableRegistry` / `tables.php`) | 5-table purpose-built schema for batch/lot/material/lot-balance/reconciliation |
| **Lot allocation** | Not modeled | **Sealed vs. Exhaust** priority-cascade allocation, with shortfall detection |
| **Shift classification** | Shift Production Log (entries only) | Explicit **Startup/Running/Finished** lifecycle classification per entry |
| **Reports** | 6 pre-built Excel reports (Apache POI) | None yet — JSON API + on-screen tables only |
| **Excel import/export** | Yes (Apache POI, desktop app) | No |
| **Automated tests** | Not present in the repo | **74 automated tests** (JUnit 5, MockMvc, Selenium) + Allure reporting |
| **Bilingual help** | Bangla/English | English/বাংলা for the UI; **6 languages** (adding Español/Français/العربية/中文) for the Help Center specifically |
| **License** | MIT | MIT |

**In short:** PPES is the broader, production-shaped system (and the one
with real auth and reporting); this project is the deeper, single-capability
one — REST-first, more rigorously tested, and the one that took the
Sealed/Exhaust and Startup/Running/Finished modeling further. Porting this
project's lot-allocation and reconciliation engine into PPES's 18-table
schema — or porting PPES's real authentication and Excel reporting back into
this project — are both natural next steps if the two are ever meant to
converge.

---

## 📂 Repository Structure

```
RbcTcsWorld_PharmaBatchLotCalc/
├── pom.xml                        Maven build (Spring Boot, JPA, Swagger, Selenium, Allure)
├── run.sh                         Convenience script — exports DB env vars, starts the app
├── README.md                      This file
├── DEVELOPMENT.md                 Detailed build log, layer-by-layer, with honesty/verification notes
├── RbcTcsWorld_Test_Cases.xlsx    ~150-case test design matrix (positive/negative/boundary/security)
│
├── src/main/java/com/sharp/batchcalc/
│   ├── BatchLotCalculator.java     Core calculation engine (framework-agnostic)
│   ├── BatchConfig, MaterialRequirement, ProductionEntry, LotBalance   Input models
│   ├── BatchHierarchy, ProgressResult, ReconciliationLine,
│   │   LotAllocationResult, LotTypeResult                              Result models
│   ├── LotStatus, LotType                                              Enums
│   ├── Main.java                    Plain console demo (no Spring)
│   ├── BatchLotCalcApplication.java Spring Boot entry point
│   ├── web/                         Stateless REST API (/api/batch/*)
│   └── persistence/                 Persisted REST API (/api/batch/records/*) + JPA entities
│
├── src/main/resources/
│   ├── application.properties       MySQL config (env-var driven)
│   └── static/index.html            The entire browser UI (single file, no build step)
│
└── src/test/java/com/sharp/batchcalc/
    ├── BatchLotCalculatorTest.java              27 unit tests
    ├── web/BatchLotControllerIntegrationTest.java        24 stateless-API tests
    ├── persistence/BatchPersistenceControllerIntegrationTest.java   ~13 persisted-API tests
    └── e2e/                          Selenium Page Object Model + CriticalJourneysE2ETest
```

---

## 🚀 Getting Started

### Prerequisites
- JDK 17+
- Maven
- MySQL 8.x (only needed for the persisted `/api/batch/records/*` API and
  the Saved Batches UI tab — everything else works without it)

### Run it

```bash
# One-time: create the database
mysql -u root -p -e "CREATE DATABASE batch_lot_calc;"

# Edit run.sh with your MySQL credentials, then:
chmod +x run.sh
./run.sh
```

Open **http://localhost:8081/** for the UI, or
**http://localhost:8081/swagger-ui.html** for the API docs.

Full setup detail, troubleshooting (including the MySQL 8
`allowPublicKeyRetrieval` and env-var-per-terminal-session gotchas), and the
plain-console-demo path are in **[DEVELOPMENT.md](DEVELOPMENT.md)**.

### Run the tests

```bash
mvn test                                     # unit + API tests (H2, no MySQL needed) — ~64 tests
mvn io.qameta.allure:allure-maven:serve      # generates + opens the Allure report

mvn spring-boot:run &                        # for the E2E layer, start the app first
mvn test -Dtest=com.sharp.batchcalc.e2e.CriticalJourneysE2ETest
```

---

## 🛠️ Tech Stack

**Backend:** Java 17, Spring Boot 3.3 (Web, Data JPA, Validation), Hibernate,
MySQL Connector/J, springdoc-openapi.

**Frontend:** Vanilla HTML/CSS/JS, no framework, no build step — served
directly by Spring Boot's static resource handler.

**Testing:** JUnit 5, Spring MockMvc, H2 (in-memory test DB), Selenium 4 +
WebDriverManager, Allure 2 (+ AspectJ weaver).

---

## Known Gaps & Honesty Notes

Documented in full in [DEVELOPMENT.md](DEVELOPMENT.md), summarized here:

- **Login/Register are demo-only** (browser localStorage), not real backend
  authentication — unlike PPES, which has working salted-SHA-256 auth shared
  across both its apps.
- No Excel import/export or pre-built reports yet (PPES has both).
- A few numeric input fields (`cartonsOrdered`, `overagePct`,
  `qtyRejected`) accept negative values with no server-side validation —
  caught and documented by the test suite rather than silently shipped.
- `DELETE /production/{entryId}` has no batch-ownership check (a cross-batch
  delete is currently possible) — flagged in
  `RbcTcsWorld_Test_Cases.xlsx` and as a regression-test TODO.
- The Español/Français/العربية/中文 Help Center translations are accurate but
  more concise than the English/বাংলা versions, and — like any translated
  regulatory content — should be reviewed by a native speaker before real
  GMP use.

## 📄 License

MIT — matching the license used on [PPES](https://github.com/rbchy/PPES-Pharmaceutical-Packaging-Management).
Add a `LICENSE` file with the standard MIT text before publishing this
repository publicly.
