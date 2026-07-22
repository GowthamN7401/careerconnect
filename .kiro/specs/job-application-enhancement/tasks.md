# Implementation Plan: Job Application Enhancement

## Overview

The backend and core frontend JS are fully implemented. The remaining work is:
CSS consolidation (move inline `<style>` blocks into the shared stylesheet),
two minor HTML fixes (broken nav link + file accept attribute), and
a set of unit tests to verify the service layer. Each task below is discrete
and independently verifiable.

---

## Tasks

- [x] 1. Add applicant-card and apply-page CSS to the shared stylesheet
  - Open `src/main/resources/static/css/style.css`
  - Append the following rule-sets that are currently duplicated as inline styles:
    - Applicant card rules: `.applicant-card`, `.applicant-header`, `.applicant-avatar`,
      `.applicant-name`, `.applicant-email`, `.detail-grid`, `.detail-item`,
      `.detail-label`, `.detail-value`, `.applicant-note`, `.resume-link`,
      `.applicant-footer`, `.applied-date`, `.status-badge`, `.page-title-bar`,
      `.empty-state`
    - Apply-page rules: `.success-banner`, `.error-banner`, `.job-meta-grid`,
      `.job-meta-item`, `.job-meta-label`, `.job-meta-value`, `.divider`,
      `.field-hint`, `.resume-area`, `#resumeFileName`
  - Copy the exact CSS property values from the inline `<style>` blocks in
    `employer-applications.html` and `job-apply.html` respectively so nothing changes visually
  - _Requirements: 2.1, 3.1, 4.1, 5.3_

- [x] 2. Remove inline `<style>` blocks from HTML files
  - In `employer-applications.html`: delete the entire `<style>…</style>` block inside `<head>`;
    the page should now rely on `css/style.css` only
  - In `job-apply.html`: delete the entire `<style>…</style>` block inside `<head>`;
    the page should now rely on `css/style.css` only
  - Verify both pages load with unchanged visual output (styles come from the shared CSS now)
  - _Requirements: 2.1, 3.1, 4.1, 5.3_

- [x] 3. Fix the broken "Apply" navbar link in `my-applications.html`
  - Open `src/main/resources/static/my-applications.html`
  - Change the second `<li>` nav item from:
    ```html
    <li><a href="job-apply.html"><i class="fa-solid fa-pen"></i> Apply</a></li>
    ```
    to:
    ```html
    <li><a href="jobseeker-dashboard.html"><i class="fa-solid fa-briefcase"></i> Browse Jobs</a></li>
    ```
  - This prevents a redirect loop where clicking "Apply" without a `jobId` immediately
    bounces the user back to the dashboard
  - _Requirements: 1.2_

- [x] 4. Fix resume file-type `accept` attribute in `register.html`
  - Open `src/main/resources/static/register.html`
  - Change the resume file input's `accept` attribute from:
    ```html
    accept="application/pdf"
    ```
    to:
    ```html
    accept=".pdf,.doc,.docx,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    ```
  - This matches Requirement 3.1, which specifies PDF, DOC, and DOCX as accepted formats
  - _Requirements: 3.1_

- [x] 5. Checkpoint — visual smoke test
  - Start the Spring Boot application (`mvn spring-boot:run`)
  - Open each page and confirm no styling regressions:
    - `job-apply.html?jobId=1` — job meta grid, resume upload area, success/error banners visible
    - `employer-applications.html?jobId=1` — applicant cards, detail grid, status badge visible
    - `my-applications.html` — navbar shows "Browse Jobs" link, not "Apply"
    - `register.html` — resume file picker accepts `.pdf`, `.doc`, `.docx`
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Write unit tests for ApplicationService
  - Create `src/test/java/jobportal/job/service/ApplicationServiceTest.java`
  - Use JUnit 5 + Mockito; mock `JobApplicationRepository` and `JobService`
  - [x] 6.1 Test `hasAlreadyApplied` returns `true` when a record with matching
        `jobId` and `seekerEmail` exists
    - _Requirements: 2.6_
  - [x] 6.2 Test `hasAlreadyApplied` returns `false` when no matching record exists
    - _Requirements: 2.6_
  - [x]* 6.3 Test `applyToJob` sets `status` to `"APPLIED"` and `appliedAt` to a
        non-null timestamp on the saved entity
    - _Requirements: 4.4_
  - [x]* 6.4 Test `applyToJob` copies the job title from the resolved `Job` onto the entity
    - _Requirements: 4.4_

- [x] 7. Write unit tests for ApplicationController duplicate-application guard
  - Create `src/test/java/jobportal/job/controller/ApplicationControllerTest.java`
  - Use `@WebMvcTest(ApplicationController.class)` with mocked services
  - [x]* 7.1 Test that `POST /api/applications` returns HTTP 400 with body
        "You have already applied for this job." when `hasAlreadyApplied` returns `true`
    - _Requirements: 2.6_
  - [x]* 7.2 Test that `GET /api/applications/{id}/resume` returns HTTP 404 when
        `resumeData` is null or empty
    - _Requirements: 6.3_

- [x] 8. Final checkpoint — run all tests
  - Run `./mvnw test` (or `mvnw.cmd test` on Windows) and verify all tests pass
  - Ensure all tests pass, ask the user if questions arise.

---

## Notes

- Tasks 1 and 2 must be done together — do not remove the inline styles before adding
  them to `style.css` or pages will lose their styling.
- Tasks 3 and 4 are independent and can be done in any order.
- Sub-tasks marked with `*` are optional and can be skipped for a faster MVP.
- All requirements references use the `Requirement.AcceptanceCriteria` numbering from
  `requirements.md`.
