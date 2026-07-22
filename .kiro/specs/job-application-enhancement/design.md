# Design Document: Job Application Enhancement

## Overview

CareerConnect is a Spring Boot job portal with a vanilla-JS frontend. This document covers
the technical design for the job-application-enhancement feature, which delivers:

1. A fixed job-details load on `job-apply.html`
2. An enhanced application form (age, degree, college, cover letter, recommendations, resume upload)
3. A success confirmation banner with auto-redirect
4. An employer-facing applicant list with full profile details and resume download
5. Remaining polish tasks: CSS consolidation, HTML fixes, and integration verification

The majority of the backend and frontend code has already been implemented. This design
documents the as-built architecture and identifies the remaining work.

---

## Architecture

The application follows a classic three-tier Spring Boot architecture:

```
Browser (static HTML + vanilla JS)
        │  REST (JSON / multipart)
        ▼
Spring Boot Controllers  (REST layer — @RestController)
        │
        ▼
Service Layer  (@Service)
        │
        ▼
JPA Repositories  (Spring Data JPA)
        │
        ▼
MySQL / H2 Database
```

All static assets (`*.html`, `css/style.css`, `js/script.js`) are served from
`src/main/resources/static` by Spring Boot's embedded Tomcat. No template engine
is used; data binding is done entirely in JavaScript via `fetch()`.

### Key design decisions

- **Single JS file (`script.js`)**: All page-specific logic is co-located in one file.
  Page detection uses DOM element presence (`document.getElementById`) rather than
  `window.location.pathname.endsWith()`, which was the root cause of the original
  job-load bug and has been corrected.
- **Resume stored as LONGBLOB**: Simplifies deployment (no file-system dependency) at
  the cost of larger DB rows. Suitable for a small-scale portal.
- **localStorage for session**: Name, email, and role are persisted in `localStorage`
  after login; no server-side session management is required.

---

## Components and Interfaces

### Backend components (already implemented)

| Component | File | Responsibility |
|---|---|---|
| `JobController` | `controller/JobController.java` | CRUD for Job entities (`/api/jobs`) |
| `ApplicationController` | `controller/ApplicationController.java` | Submit, query, and download applications (`/api/applications`) |
| `UserController` | `controller/UserController.java` | Register and login (`/users/register`, `/users/login`) |
| `ApplicationService` | `service/ApplicationService.java` | Application business logic, duplicate detection, timestamp |
| `JobService` | `service/JobService.java` | Job retrieval and creation |
| `UserService` | `service/UserService.java` | User lookup and role validation |
| `JobApplicationEntity` | `model/JobApplicationEntity.java` | JPA entity for `job_applications` table |
| `JobApplicationRepository` | `repository/JobApplicationRepository.java` | Spring Data queries for applications |

### REST API surface

#### Job endpoints

| Method | Path | Request | Response | Notes |
|---|---|---|---|---|
| `GET` | `/api/jobs` | — | `Job[]` (200) | Public list |
| `GET` | `/api/jobs/{id}` | — | `Job` (200) / 404 | Used by apply page |
| `GET` | `/api/jobs/employer?email=` | — | `Job[]` (200) | Employer dashboard |
| `POST` | `/api/jobs` | `JobRequest` (JSON) | `Job` (200) / 400 | Employer posts job |

#### Application endpoints

| Method | Path | Request | Response | Notes |
|---|---|---|---|---|
| `POST` | `/api/applications` | `multipart/form-data` | `JobApplicationEntity` (200) / 400 | Submit application |
| `GET` | `/api/applications?jobId=` | — | `JobApplicationEntity[]` (200) | Employer view |
| `GET` | `/api/applications?seekerEmail=` | — | `JobApplicationEntity[]` (200) | Seeker view |
| `GET` | `/api/applications/{id}/resume` | — | `byte[]` (200) / 404 | File download |

#### User endpoints

| Method | Path | Request | Response | Notes |
|---|---|---|---|---|
| `POST` | `/users/register` | `multipart/form-data` | text (200) | Registration |
| `POST` | `/users/login` | `multipart/form-data` | `LoginResponse` (200) | Sets localStorage |

### Frontend components (already implemented)

| File | Pages served | Key functions |
|---|---|---|
| `js/script.js` | All pages | `renderJobSeekerDashboard`, `loadJobDetails`, `loadEmployerApplicants`, `loadSeekerApplications`, `loadEmployerJobs`, auth guard |
| `job-apply.html` | Apply page | Form layout, success/error banners, file upload UI |
| `employer-applications.html` | Employer applicant list | Applicant card grid with inline CSS |
| `my-applications.html` | Seeker application history | Application cards |
| `jobseeker-dashboard.html` | Job listing | Search bar, job cards |
| `employer-dashboard.html` | Employer panel | Posted jobs, job-post form |

### Remaining gaps

1. **CSS consolidation** — `employer-applications.html` and `job-apply.html` each contain
   a `<style>` block with classes used only by those pages. These classes must be moved
   into the shared `css/style.css` to avoid duplication and simplify future maintenance.
   The affected classes are:
   - From `employer-applications.html`: `.applicant-card`, `.applicant-header`,
     `.applicant-avatar`, `.applicant-name`, `.applicant-email`, `.detail-grid`,
     `.detail-item`, `.detail-label`, `.detail-value`, `.applicant-note`, `.resume-link`,
     `.applicant-footer`, `.applied-date`, `.status-badge`, `.page-title-bar`,
     `.empty-state`
   - From `job-apply.html`: `.success-banner`, `.error-banner`, `.job-meta-grid`,
     `.job-meta-item`, `.job-meta-label`, `.job-meta-value`, `.divider`, `.field-hint`,
     `.resume-area`, `#resumeFileName`

2. **Broken navbar link in `my-applications.html`** — The "Apply" nav link points to
   `job-apply.html` with no `jobId` parameter. `loadJobDetails()` will redirect the
   user immediately to `jobseeker-dashboard.html`, making the link useless. It should
   be changed to "Browse Jobs" pointing to `jobseeker-dashboard.html`.

3. **Resume `accept` attribute in `register.html`** — The file input only accepts
   `application/pdf`. Requirement 3.1 specifies PDF, DOC, and DOCX. The `accept`
   attribute must be updated to `.pdf,.doc,.docx`.

---

## Data Models

### `job_applications` table (mapped by `JobApplicationEntity`)

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT PK | Auto-generated |
| `job_id` | BIGINT | FK to jobs table |
| `job_title` | VARCHAR | Denormalized for display |
| `seeker_id` | BIGINT | FK to users table |
| `seeker_name` | VARCHAR | Stored at submission time |
| `seeker_email` | VARCHAR | Used for duplicate detection |
| `age` | INT | Required by Requirement 2 |
| `degree` | VARCHAR | Required by Requirement 2 |
| `college` | VARCHAR | Required by Requirement 2 |
| `recommendations` | VARCHAR(1000) | Optional |
| `cover_letter` | VARCHAR(2000) | Optional |
| `resume_file_name` | VARCHAR | Original filename |
| `resume_data` | LONGBLOB | File bytes |
| `applied_at` | DATETIME | Set by service layer |
| `status` | VARCHAR | Default "APPLIED" |

### `localStorage` session model (browser)

| Key | Value |
|---|---|
| `name` | Logged-in user's full name |
| `email` | Logged-in user's email address |
| `role` | `"JOB_SEEKER"` or `"EMPLOYER"` |

---

## Correctness Properties

This feature is predominantly UI rendering, HTML fixes, and CSS consolidation — categories
where property-based testing does not apply (no pure functions with universally quantifiable
input/output behavior). The backend logic (`ApplicationService`, `JobController`) involves
database I/O and external state, making integration tests and example-based unit tests more
appropriate than property-based testing.

The Correctness Properties section is intentionally omitted. See Testing Strategy below
for the appropriate test approach.

---

## Error Handling

| Scenario | Component | Behaviour |
|---|---|---|
| `jobId` absent from URL | `loadJobDetails()` | Immediate redirect to `jobseeker-dashboard.html` |
| `GET /api/jobs/{id}` returns non-2xx | `loadJobDetails()` | Show error banner, redirect after 2.5 s |
| Required field missing on apply form | `applyForm` submit handler | Show `#errorBanner` with per-field messages; do not submit |
| MIME type not PDF/DOC/DOCX on apply | `applyForm` submit handler | Show error message; do not submit |
| Duplicate application (same `jobId` + `seekerEmail`) | `ApplicationController` | Return HTTP 400 "You have already applied for this job." |
| Applicant account not found | `ApplicationController` | Return HTTP 400 "Applicant account not found." |
| Resume not stored for application ID | `ApplicationController` `/resume` endpoint | Return HTTP 404 |
| No `jobId` on `employer-applications.html` | `loadEmployerApplicants()` | Alert + redirect to `employer-dashboard.html` |
| No applications for a job | `loadEmployerApplicants()` | Render empty-state message |

---

## Testing Strategy

Because the remaining implementation work is CSS migration and minor HTML attribute fixes
rather than algorithmic logic, the appropriate testing approach is example-based and
end-to-end.

### Unit tests

- Verify `ApplicationService.hasAlreadyApplied()` returns `true` when a matching record
  exists and `false` when none exists (use an in-memory H2 database).
- Verify `ApplicationService.applyToJob()` sets `status = "APPLIED"` and `appliedAt`
  to a non-null timestamp.
- Verify `ApplicationController` returns HTTP 400 on duplicate application.
- Verify `ApplicationController` returns HTTP 404 on resume download when `resumeData`
  is null.

### Integration / smoke tests (manual or Selenium)

1. **Register → Login → Browse → Apply** end-to-end flow:
   - Register as JOB_SEEKER with a PDF resume.
   - Log in and verify redirect to `jobseeker-dashboard.html`.
   - Click "View & Apply" on a job and verify job details populate.
   - Fill in age, degree, college, upload a resume, submit.
   - Verify success banner appears and page redirects to `my-applications.html`.
   - Verify the submitted application card appears on `my-applications.html`.

2. **Employer post + view applicants** flow:
   - Log in as EMPLOYER, post a job.
   - Verify job appears on `employer-dashboard.html`.
   - Click "View Applicants" and verify redirect to `employer-applications.html?jobId=...`.
   - Verify applicant card shows name, email, age, degree, college, resume download link,
     applied date, and status badge.

3. **Duplicate application guard**:
   - Apply for a job, then attempt to apply for the same job again.
   - Verify the error banner shows "You have already applied for this job."

4. **Resume download**:
   - Click "Download Resume" on an applicant card.
   - Verify the browser triggers a file download with the correct filename.

5. **CSS consolidation verification**:
   - Remove `<style>` blocks from `employer-applications.html` and `job-apply.html`.
   - Verify all pages still render correctly (no missing styles).

### Test framework

Java unit tests use JUnit 5 with Mockito for service-layer tests and
Spring Boot Test (`@SpringBootTest`) with an H2 in-memory database for
controller-layer integration tests.
