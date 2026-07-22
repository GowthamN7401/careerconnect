# Requirements Document

## Introduction

The CareerConnect job portal currently has a broken job application flow. When a job seeker clicks "View & Apply" on a job listing, the `job-apply.html` page shows "Unable to load this job" because the frontend calls `/api/jobs/{id}` but the job API does not reliably return a response the page can consume (root cause: the `job-apply.html` page uses `window.location.pathname.endsWith("job-apply.html")` which fails in some server configurations, and the `jobDetails` div is present on `job-apply.html` causing `loadJobDetails()` to be triggered, but the fetch can fail due to CORS or path resolution issues).

Beyond the bug fix, the feature adds:
1. An enhanced application form collecting full applicant profile details and a resume upload
2. A "Applied Successfully" confirmation message after submission
3. An employer view displaying all applicants for a posted job, including all submitted profile details

## Glossary

- **Job_Portal**: The CareerConnect Spring Boot web application
- **Job_Seeker**: A registered user with the `JOB_SEEKER` role
- **Employer**: A registered user with the `EMPLOYER` role
- **Job**: A job posting created by an Employer
- **Application**: A submission by a Job_Seeker for a specific Job
- **Application_Form**: The HTML form on `job-apply.html` used to submit an Application
- **Applicant_Profile**: The set of details collected from a Job_Seeker during application: full name, age, degree, college name, additional recommendations or notes, and resume file
- **Application_Controller**: The Spring REST controller at `/api/applications`
- **Job_Controller**: The Spring REST controller at `/api/jobs`
- **JobApplicationEntity**: The JPA entity persisting an Application to the database
- **JobApplicationRequest**: The DTO carrying application submission data to the backend
- **ApplicationService**: The Spring service layer handling application business logic
- **Employer_Applicants_Page**: The `employer-applications.html` page showing all applicants for a selected Job

---

## Requirements

### Requirement 1: Fix Job Loading on the Apply Page

**User Story:** As a Job_Seeker, I want the job details to display correctly when I click "View & Apply", so that I can read the job description before submitting my application.

#### Acceptance Criteria

1. WHEN a Job_Seeker navigates to `job-apply.html?jobId={id}`, THE Job_Portal SHALL fetch the job from `/api/jobs/{id}` and render the job title, company, location, department, employment type, salary, description, and employer name on the page.
2. IF the `jobId` query parameter is absent from the URL, THEN THE Job_Portal SHALL redirect the Job_Seeker to `jobseeker-dashboard.html`.
3. IF the `/api/jobs/{id}` request returns a non-2xx HTTP status, THEN THE Job_Portal SHALL display the message "Unable to load this job." and redirect the Job_Seeker to `jobseeker-dashboard.html`.
4. WHEN the job details are loaded successfully, THE Job_Portal SHALL pre-fill the applicant name and email fields in the Application_Form using values stored in `localStorage`.
5. THE Job_Controller SHALL return the Job as a JSON response body with HTTP 200 for a valid `id`, and HTTP 404 when no Job with that `id` exists.

---

### Requirement 2: Enhanced Application Form with Applicant Profile Details

**User Story:** As a Job_Seeker, I want to fill in my full profile details when applying for a job, so that the employer receives complete information about me.

#### Acceptance Criteria

1. THE Application_Form SHALL contain the following fields: full name (read-only, pre-filled), email (read-only, pre-filled), age (numeric, required), degree (text, required), college name (text, required), additional recommendations or notes (textarea, optional), and resume file upload (required, accepting PDF and DOC/DOCX file types).
2. WHEN a Job_Seeker submits the Application_Form, THE Job_Portal SHALL validate that age, degree, college name, and resume file are provided before sending the request to the backend.
3. IF any required field is missing on form submission, THEN THE Job_Portal SHALL display a field-level or summary validation message and SHALL NOT submit the request.
4. THE JobApplicationRequest SHALL include fields for: `jobId`, `seekerName`, `seekerEmail`, `age`, `degree`, `college`, `recommendations`, and `resumeFileName`.
5. THE JobApplicationEntity SHALL persist the following fields in the `job_applications` database table: `age`, `degree`, `college`, `recommendations`, and `resume_file_name`.
6. WHEN a duplicate application is detected (same `jobId` and `seekerEmail`), THE Application_Controller SHALL return HTTP 400 with the message "You have already applied for this job."

---

### Requirement 3: Resume File Upload During Application

**User Story:** As a Job_Seeker, I want to upload my resume when applying, so that the employer can review my qualifications.

#### Acceptance Criteria

1. WHEN a Job_Seeker selects a resume file in the Application_Form, THE Job_Portal SHALL accept files with MIME types `application/pdf`, `application/msword`, and `application/vnd.openxmlformats-officedocument.wordprocessingml.document`.
2. IF a Job_Seeker selects a file with an unsupported MIME type, THEN THE Job_Portal SHALL display the message "Only PDF and Word documents are accepted." and SHALL NOT submit the form.
3. THE Application_Controller SHALL accept the application submission as a `multipart/form-data` request containing the resume file and the application fields.
4. THE ApplicationService SHALL store the resume file bytes and original filename in the JobApplicationEntity.
5. THE JobApplicationEntity SHALL include fields `resumeData` (LONGBLOB) and `resumeFileName` (VARCHAR) to persist the uploaded file.

---

### Requirement 4: Success Confirmation After Submission

**User Story:** As a Job_Seeker, I want to see a "Applied Successfully" confirmation after submitting my application, so that I know my submission was received.

#### Acceptance Criteria

1. WHEN the Application_Controller returns HTTP 200 after a successful application submission, THE Job_Portal SHALL display the message "Applied Successfully" to the Job_Seeker.
2. WHEN the success message is displayed, THE Job_Portal SHALL redirect the Job_Seeker to `my-applications.html` after a 2-second delay.
3. IF the application submission returns an error response, THEN THE Job_Portal SHALL display the error message returned by the server and SHALL NOT redirect.
4. WHEN the Application_Controller saves an Application successfully, THE ApplicationService SHALL set the `status` field to "APPLIED" and the `appliedAt` field to the current server timestamp.

---

### Requirement 5: Employer Applicant List with Full Profile Details

**User Story:** As an Employer, I want to see all applicants who applied to my job posting along with their complete profile details, so that I can evaluate candidates.

#### Acceptance Criteria

1. WHEN an Employer clicks "View Applicants" for a job on `employer-dashboard.html`, THE Job_Portal SHALL navigate to `employer-applications.html?jobId={id}`.
2. WHEN `employer-applications.html` loads with a valid `jobId` query parameter, THE Employer_Applicants_Page SHALL fetch all applications from `/api/applications?jobId={id}` and render one card per applicant.
3. THE Employer_Applicants_Page SHALL display the following for each applicant: full name, email, age, degree, college, recommendations, resume filename (as a download link if data is available), applied date, and application status.
4. IF no applications exist for the given `jobId`, THEN THE Employer_Applicants_Page SHALL display the message "No applications have been submitted for this job yet."
5. THE Application_Controller GET endpoint SHALL return all JobApplicationEntity records matching the given `jobId` as a JSON array.
6. WHEN an Employer accesses `employer-applications.html` without a `jobId` query parameter, THE Job_Portal SHALL redirect the Employer to `employer-dashboard.html`.

---

### Requirement 6: Resume Download for Employers

**User Story:** As an Employer, I want to download the resume of an applicant, so that I can review their full qualifications offline.

#### Acceptance Criteria

1. THE Job_Portal SHALL expose a GET endpoint at `/api/applications/{id}/resume` that returns the resume file bytes with the original filename as the `Content-Disposition` header value.
2. WHEN a Job_Seeker's resume is stored in the database, THE Application_Controller SHALL return HTTP 200 with the file content and the correct `Content-Type` header matching the uploaded file type.
3. IF no resume is stored for the given application `id`, THEN THE Application_Controller SHALL return HTTP 404.
4. WHEN the Employer_Applicants_Page renders an applicant card that includes a resume, THE Job_Portal SHALL render a hyperlink pointing to `/api/applications/{id}/resume` with the label "Download Resume".
