# Clinic Patient & Appointment REST API (Spring Boot)

Conversion of the original JSF + Hibernate clinic app into a Spring Boot REST API. A third
entity, **Doctor**, was added alongside **Patient** and **Appointment**, with full CRUD on all
three and appointment scheduling rules enforced server-side.

## What was implemented

- `Patient`, `Doctor`, `Appointment` JPA entities (`src/main/java/com/clinic/api/entity`), each
  with Bean Validation constraints (required fields, phone format, string lengths, date-of-birth
  in the past, enum-style `Pattern` checks for gender/status).
- REST controllers for each entity (`.../controller`) exposing standard CRUD endpoints.
- Service layer (`.../service`) holding the business rules that don't fit on a single field —
  uniqueness checks, scheduling conflicts, availability windows, delete guards.
- A single `GlobalExceptionHandler` mapping validation failures, not-found lookups, and business
  rule violations to consistent JSON error bodies (`ApiError`) with the right HTTP status.
- `DataSeeder` populates 2 doctors and 2 patients on startup so the API is exercisable
  immediately.
- `Clinic-API.postman_collection.json` — a Postman collection covering CRUD and every business
  rule, positive and negative.

### Entity relationships

```
Patient (1) ────< Appointment >──── (1) Doctor
   patientId                            doctorId
```

An `Appointment` references exactly one `Patient` and one `Doctor` by id
(`AppointmentRequest.patientId` / `doctorId`); both are required and resolved server-side before
the appointment is persisted.

## Stack

- Java 11, Spring Boot 2.7 (Web, Data JPA, Validation)
- PostgreSQL (active by default), H2 in-memory config included as a commented-out alternative
- Maven

## How to run

### Option A — PostgreSQL (default, active in `application.yml`)

1. Create a local database matching the configured connection:
   ```sql
   CREATE DATABASE clinicdb;
   ```
   `application.yml` connects with `jdbc:postgresql://localhost:5432/clinicdb`, user `postgres`,
   password `password` — adjust to match your local Postgres install.
2. Run the app:
   ```bash
   mvn spring-boot:run
   ```
   `ddl-auto: update` creates/updates the schema automatically on startup.

### Option B — H2 in-memory (zero setup)

In `src/main/resources/application.yml`, comment out the active PostgreSQL `datasource`/`jpa`
block and uncomment the H2 block below it. Then:
```bash
mvn spring-boot:run
```
H2 console is available at `http://localhost:8080/h2-console` (JDBC URL
`jdbc:h2:mem:clinicdb;DB_CLOSE_DELAY=-1`, user `sa`, empty password). No separate `pom.xml`
change is needed — the H2 dependency is already present alongside the PostgreSQL driver.

Either way, the app starts on `http://localhost:8080` and seeds 2 sample doctors and 2 sample
patients on startup (`DataSeeder`).

```bash
mvn test           # run tests
mvn clean package  # build the jar
```

## Endpoints

| Method | Path                     | Purpose                      |
|--------|--------------------------|-------------------------------|
| GET    | `/api/patients`          | List all patients             |
| GET    | `/api/patients/{id}`     | Get one patient                |
| POST   | `/api/patients`          | Create a patient               |
| PUT    | `/api/patients/{id}`     | Update a patient                |
| DELETE | `/api/patients/{id}`     | Delete a patient                |
| GET    | `/api/doctors`           | List all doctors              |
| GET    | `/api/doctors/{id}`      | Get one doctor                 |
| POST   | `/api/doctors`           | Create a doctor                |
| PUT    | `/api/doctors/{id}`      | Update a doctor                 |
| DELETE | `/api/doctors/{id}`      | Delete a doctor                 |
| GET    | `/api/appointments`      | List all appointments         |
| GET    | `/api/appointments/{id}` | Get one appointment            |
| POST   | `/api/appointments`      | Create an appointment (by patientId/doctorId) |
| PUT    | `/api/appointments/{id}` | Update an appointment           |
| DELETE | `/api/appointments/{id}` | Delete an appointment           |

## Business rules

Field-level validation (`@NotBlank`, `@Pattern`, `@Past`, `@Size`, etc. on the entities/DTOs)
returns **HTTP 400**, handled centrally by `GlobalExceptionHandler.handleValidation`. Unknown
ids return **HTTP 404** via `ResourceNotFoundException`. Cross-record rules are enforced in the
service layer and return **HTTP 409** via `BusinessRuleException`:

| Rule | Enforced in | HTTP if violated |
|---|---|---|
| Patient phone number must be unique | `PatientService.assertPhoneUnique` | 409 |
| Patient with a Scheduled appointment can't be deleted | `PatientService.delete` | 409 |
| Doctor phone number must be unique | `DoctorService.assertPhoneUnique` | 409 |
| Doctor's availability start hour must be before end hour | `DoctorService.assertAvailabilityValid` | 409 |
| Doctor with a Scheduled appointment can't be deleted | `DoctorService.delete` | 409 |
| Appointment can't be in the past | `AppointmentService.validateBusinessRules` | 409 |
| Appointment time must fall inside the assigned doctor's availability window | `AppointmentService.validateBusinessRules` | 409 |
| No double-booking: same doctor + date + time + status=Scheduled | `AppointmentService.validateBusinessRules` | 409 |
| Referenced patient/doctor id must exist | `AppointmentService.create`/`update` | 404 |

## How it was tested

`Clinic-API.postman_collection.json` is organized into three folders — **Patients**,
**Doctors**, **Appointments** — each with:

- **Positive cases**: list, get-by-id, create, update, delete, covering the full CRUD cycle for
  that entity.
- **Negative cases**, one per business rule, each asserting the expected status code:
  - Patients: missing required fields (400), duplicate phone (409), delete with an active
    appointment (409).
  - Doctors: invalid availability window where start ≥ end (409), delete with an active
    appointment (409).
  - Appointments: missing reason (400), date/time in the past (409), time outside doctor
    availability (409), double-booking the same doctor/date/time (409), unknown patient id (404).

Import the collection into Postman and set the `baseUrl` collection variable (defaults to
`http://localhost:8080`). Every request in the collection, both positive and negative, was run
against a live `mvn spring-boot:run` instance before delivery.

## Sample calls

### Successful creation — `POST /api/appointments`

Request:
```json
{
  "patientId": 1,
  "doctorId": 1,
  "appointmentDate": "2026-12-01",
  "appointmentTime": "10:00:00",
  "reason": "Annual checkup",
  "status": "Scheduled"
}
```

Response — `201 Created`:
```json
{
  "appointmentId": 3,
  "patient": {
    "patientId": 1,
    "firstName": "Eric",
    "lastName": "Niyonzima",
    "dateOfBirth": "1995-03-12",
    "gender": "Male",
    "phone": "0722334455",
    "email": "eric@example.com",
    "address": "Kigali, Rwanda"
  },
  "doctor": {
    "doctorId": 1,
    "fullName": "Dr. Alice Uwimana",
    "specialization": "Cardiology",
    "phone": "0788123456",
    "availabilityStartHour": 8,
    "availabilityEndHour": 16
  },
  "appointmentDate": "2026-12-01",
  "appointmentTime": "10:00:00",
  "reason": "Annual checkup",
  "status": "Scheduled"
}
```

### Business rule violation — double-booking, `POST /api/appointments`

Request (same doctor/date/time as an existing Scheduled appointment):
```json
{
  "patientId": 2,
  "doctorId": 1,
  "appointmentDate": "2026-12-01",
  "appointmentTime": "10:00:00",
  "reason": "Follow-up",
  "status": "Scheduled"
}
```

Response — `409 Conflict`:
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Doctor already has a scheduled appointment at this date and time",
  "details": []
}
```
