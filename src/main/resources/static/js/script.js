const apiBase = "";

// ─── Password toggle ─────────────────────────────────────────────────────────
const passwordToggle = document.getElementById("togglePassword");
const passwordInput  = document.getElementById("password");
if (passwordToggle && passwordInput) {
    passwordToggle.addEventListener("click", () => {
        if (passwordInput.type === "password") {
            passwordInput.type = "text";
            passwordToggle.classList.replace("fa-eye", "fa-eye-slash");
        } else {
            passwordInput.type = "password";
            passwordToggle.classList.replace("fa-eye-slash", "fa-eye");
        }
    });
}

// ─── Logout ───────────────────────────────────────────────────────────────────
document.querySelectorAll("#logoutLink").forEach(link => {
    link.addEventListener("click", () => {
        localStorage.removeItem("name");
        localStorage.removeItem("email");
        localStorage.removeItem("role");
    });
});

// ─── Resume field visibility on register page ─────────────────────────────────
const resumeInputRow = document.getElementById("resumeInputRow");
document.querySelectorAll('input[name="role"]').forEach(input => {
    input.addEventListener("change", () => {
        if (resumeInputRow) {
            resumeInputRow.style.display =
                document.querySelector('input[name="role"]:checked')?.value === "JOB_SEEKER"
                    ? "flex" : "none";
        }
    });
});
if (resumeInputRow) {
    resumeInputRow.style.display =
        document.querySelector('input[name="role"]:checked')?.value === "JOB_SEEKER"
            ? "flex" : "none";
}

// ─── Auth guard ───────────────────────────────────────────────────────────────
function requireAuth(roleRequired) {
    const email = localStorage.getItem("email");
    const role  = localStorage.getItem("role");
    if (!email || !role) { window.location.href = "login.html"; return null; }
    if (roleRequired && role !== roleRequired) { window.location.href = "login.html"; return null; }
    const welcome = document.getElementById("welcomeName");
    if (welcome) welcome.textContent = `Welcome back, ${localStorage.getItem("name") || "User"}`;
    return { email, role };
}

// ─── Login ────────────────────────────────────────────────────────────────────
const loginForm = document.getElementById("loginForm");
if (loginForm) {
    loginForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const email = document.getElementById("email").value.trim();
        const pass  = document.getElementById("password").value.trim();
        const role  = document.querySelector('input[name="role"]:checked').value;
        if (!email || !pass) { alert("Please enter email and password."); return; }
        try {
            const fd = new FormData();
            fd.append("email", email);
            fd.append("password", pass);
            fd.append("role", role);
            const res    = await fetch(`${apiBase}/users/login`, { method: "POST", body: fd });
            const result = await res.json();
            if (result.success) {
                localStorage.setItem("name",  result.name);
                localStorage.setItem("email", result.email);
                localStorage.setItem("role",  result.role);
                window.location.href = result.role === "JOB_SEEKER"
                    ? "jobseeker-dashboard.html" : "employer-dashboard.html";
            } else {
                alert(result.message);
            }
        } catch (err) {
            console.error(err);
            alert("Unable to connect to the server. Please try again later.");
        }
    });
}

// ─── Register ─────────────────────────────────────────────────────────────────
const registerForm = document.getElementById("registerForm");
if (registerForm) {
    registerForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const name            = document.getElementById("name").value.trim();
        const email           = document.getElementById("email").value.trim();
        const password        = document.getElementById("password").value.trim();
        const confirmPassword = document.getElementById("confirmPassword").value.trim();
        const role            = document.querySelector('input[name="role"]:checked').value;
        const resumeFile      = document.getElementById("resume")?.files?.[0];

        if (!name || !email || !password || !confirmPassword) {
            alert("Please fill in every field."); return;
        }
        if (password !== confirmPassword) {
            alert("Passwords do not match."); return;
        }
        if (role === "JOB_SEEKER" && !resumeFile) {
            alert("Please upload your resume to complete registration."); return;
        }

        const fd = new FormData();
        fd.append("name",     name);
        fd.append("email",    email);
        fd.append("password", password);
        fd.append("role",     role);
        if (resumeFile) fd.append("resume", resumeFile);

        try {
            const res        = await fetch(`${apiBase}/users/register`, { method: "POST", body: fd });
            const resultText = await res.text();
            alert(resultText);
            if (res.ok && resultText === "Registration Successful") {
                window.location.href = "login.html";
            }
        } catch (err) {
            console.error(err);
            alert("Unable to register at this time. Please try again.");
        }
    });
}

// ─── Fetch all jobs ───────────────────────────────────────────────────────────
async function fetchJobs() {
    const res = await fetch(`${apiBase}/api/jobs`);
    return res.json();
}

// ─── Job card (seeker dashboard) ──────────────────────────────────────────────
function createJobCard(job) {
    const card = document.createElement("article");
    card.className = "card job-card";

    const title = document.createElement("h3");
    title.textContent = job.title;
    card.appendChild(title);

    const company = document.createElement("p");
    company.textContent = `${job.company} · ${job.location}`;
    card.appendChild(company);

    const badges = document.createElement("div");
    badges.className = "job-badges";
    [job.department, job.employmentType, job.salary].forEach(text => {
        if (text) {
            const b = document.createElement("span");
            b.className = "badge";
            b.textContent = text;
            badges.appendChild(b);
        }
    });
    card.appendChild(badges);

    const desc = document.createElement("p");
    desc.textContent = job.description
        ? job.description.substring(0, 140) + (job.description.length > 140 ? "…" : "")
        : "";
    card.appendChild(desc);

    const btn = document.createElement("button");
    btn.type = "button";
    btn.innerHTML = '<i class="fa-solid fa-arrow-right"></i> View & Apply';
    btn.addEventListener("click", () => {
        window.location.href = `job-apply.html?jobId=${job.id}`;
    });
    card.appendChild(btn);
    return card;
}

// ─── Job Seeker Dashboard ────────────────────────────────────────────────────
async function renderJobSeekerDashboard() {
    const auth = requireAuth("JOB_SEEKER");
    if (!auth) return;
    const allJobs   = await fetchJobs();
    const jobList   = document.getElementById("jobList");
    const countSpan = document.getElementById("jobCount");
    jobList.innerHTML = "";
    allJobs.forEach(job => jobList.appendChild(createJobCard(job)));
    if (countSpan) countSpan.textContent = allJobs.length;

    const searchInput = document.getElementById("jobSearch");
    if (searchInput) {
        searchInput.addEventListener("input", () => {
            const filter = searchInput.value.toLowerCase();
            jobList.innerHTML = "";
            const filtered = allJobs.filter(job =>
                [job.title, job.company, job.location, job.department, job.employmentType]
                    .filter(Boolean).some(v => v.toLowerCase().includes(filter))
            );
            filtered.forEach(job => jobList.appendChild(createJobCard(job)));
            if (countSpan) countSpan.textContent = filtered.length;
        });
    }
}

// ─── Employer Dashboard ──────────────────────────────────────────────────────
async function loadEmployerJobs() {
    const auth = requireAuth("EMPLOYER");
    if (!auth) return;
    const res  = await fetch(`${apiBase}/api/jobs/employer?email=${encodeURIComponent(auth.email)}`);
    const jobs = await res.json();
    const list = document.getElementById("postedJobsList");
    const count = document.getElementById("employerJobCount");
    list.innerHTML = "";
    if (count) count.textContent = `${jobs.length} open position${jobs.length === 1 ? "" : "s"}`;

    jobs.forEach(job => {
        const card = document.createElement("article");
        card.className = "card job-card";

        const title = document.createElement("h3");
        title.textContent = job.title;
        card.appendChild(title);

        const details = document.createElement("p");
        details.textContent = `${job.company} · ${job.location}`;
        card.appendChild(details);

        const meta = document.createElement("div");
        meta.className = "job-badges";
        [job.department, job.employmentType, job.salary].forEach(text => {
            if (text) {
                const b = document.createElement("span");
                b.className = "badge";
                b.textContent = text;
                meta.appendChild(b);
            }
        });
        card.appendChild(meta);

        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "view-applications-button";
        btn.innerHTML = '<i class="fa-solid fa-users"></i> View Applicants';
        btn.addEventListener("click", () => {
            window.location.href = `employer-applications.html?jobId=${job.id}`;
        });
        card.appendChild(btn);
        list.appendChild(card);
    });
}

// ─── Post Job Form ────────────────────────────────────────────────────────────
const jobPostForm = document.getElementById("jobPostForm");
if (jobPostForm) {
    jobPostForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const auth = requireAuth("EMPLOYER");
        if (!auth) return;

        const title          = document.getElementById("jobTitle").value.trim();
        const company        = document.getElementById("company").value.trim();
        const location       = document.getElementById("location").value.trim();
        const salary         = document.getElementById("salary").value.trim();
        const department     = document.getElementById("department").value.trim();
        const employmentType = document.getElementById("employmentType").value;
        const description    = document.getElementById("description").value.trim();

        if (!title || !company || !location || !salary || !department || !description) {
            alert("Please fill in all required fields."); return;
        }

        try {
            const res = await fetch(`${apiBase}/api/jobs`, {
                method:  "POST",
                headers: { "Content-Type": "application/json" },
                body:    JSON.stringify({
                    title, company, location, salary, department, employmentType, description,
                    employerName:  localStorage.getItem("name"),
                    employerEmail: auth.email
                })
            });
            if (!res.ok) {
                const msg = await res.text();
                alert(msg || "Unable to post job."); return;
            }
            await loadEmployerJobs();
            jobPostForm.reset();
            alert("Job posted successfully.");
        } catch (err) {
            console.error(err);
            alert("Unable to connect to the server. Please try again later.");
        }
    });
}

// ─── Load Job Details & Application Form ─────────────────────────────────────
async function loadJobDetails() {
    const auth = requireAuth("JOB_SEEKER");
    if (!auth) return;

    const params = new URLSearchParams(window.location.search);
    const jobId  = params.get("jobId");

    if (!jobId || isNaN(Number(jobId)) || Number(jobId) <= 0) {
        window.location.href = "jobseeker-dashboard.html";
        return;
    }

    let job;
    try {
        const res = await fetch(`${apiBase}/api/jobs/${jobId}`);
        if (!res.ok) {
            const errBanner = document.getElementById("errorBanner");
            if (errBanner) {
                errBanner.textContent = "Unable to load this job. Redirecting...";
                errBanner.style.display = "block";
            }
            setTimeout(() => { window.location.href = "jobseeker-dashboard.html"; }, 2500);
            return;
        }
        job = await res.json();
    } catch (err) {
        console.error(err);
        window.location.href = "jobseeker-dashboard.html";
        return;
    }

    // Render job header
    const heading = document.getElementById("jobTitleHeading");
    if (heading) heading.textContent = `Apply for: ${job.title}`;

    // Render job meta grid
    const details = document.getElementById("jobDetails");
    if (details) {
        const fields = [
            { label: "Company",         value: job.company },
            { label: "Location",        value: job.location },
            { label: "Department",      value: job.department },
            { label: "Employment Type", value: job.employmentType },
            { label: "Salary",          value: job.salary },
            { label: "Posted by",       value: job.employerName },
        ];
        details.innerHTML = fields.map(f => `
            <div class="job-meta-item">
                <span class="job-meta-label">${f.label}</span>
                <span class="job-meta-value">${f.value || "—"}</span>
            </div>
        `).join("");

        if (job.description) {
            details.innerHTML += `
                <div class="job-meta-item" style="grid-column:1/-1">
                    <span class="job-meta-label">Description</span>
                    <span class="job-meta-value" style="white-space:pre-line">${job.description}</span>
                </div>`;
        }
    }

    // Pre-fill name/email from localStorage
    const nameInput  = document.getElementById("applicantName");
    const emailInput = document.getElementById("applicantEmail");
    if (nameInput)  nameInput.value  = localStorage.getItem("name")  || "";
    if (emailInput) emailInput.value = auth.email;

    // Resume file name preview
    const resumeUpload   = document.getElementById("resumeUpload");
    const resumeFileName = document.getElementById("resumeFileName");
    if (resumeUpload && resumeFileName) {
        resumeUpload.addEventListener("change", () => {
            resumeFileName.textContent = resumeUpload.files[0]
                ? resumeUpload.files[0].name : "No file chosen";
        });
    }

    // Application form submission
    const applyForm = document.getElementById("applyForm");
    if (!applyForm) return;

    applyForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const age      = document.getElementById("applicantAge").value.trim();
        const degree   = document.getElementById("applicantDegree").value.trim();
        const college  = document.getElementById("applicantCollege").value.trim();
        const cover    = document.getElementById("coverLetter").value.trim();
        const recs     = document.getElementById("recommendations").value.trim();
        const file     = resumeUpload?.files?.[0];

        // Validation
        const errors = [];
        if (!age || isNaN(age) || Number(age) < 16 || Number(age) > 100) {
            errors.push("Age must be a number between 16 and 100.");
        }
        if (!degree) errors.push("Degree is required.");
        if (!college) errors.push("College/University is required.");
        if (!file) errors.push("Please upload your resume (PDF or Word).");

        if (errors.length > 0) {
            const errBanner = document.getElementById("errorBanner");
            errBanner.innerHTML = errors.map(msg => `<div>⚠ ${msg}</div>`).join("");
            errBanner.style.display = "block";
            errBanner.scrollIntoView({ behavior: "smooth", block: "nearest" });
            return;
        }

        const submitBtn = document.getElementById("submitBtn");
        if (submitBtn) { submitBtn.disabled = true; submitBtn.textContent = "Submitting..."; }

        const fd = new FormData();
        fd.append("jobId",           jobId);
        fd.append("seekerName",      localStorage.getItem("name") || "");
        fd.append("seekerEmail",     auth.email);
        fd.append("age",             age);
        fd.append("degree",          degree);
        fd.append("college",         college);
        fd.append("coverLetter",     cover);
        fd.append("recommendations", recs);
        fd.append("resume",          file);

        try {
            const res = await fetch(`${apiBase}/api/applications`, {
                method: "POST",
                body:   fd
            });

            const errBanner     = document.getElementById("errorBanner");
            const successBanner = document.getElementById("successBanner");

            if (res.ok) {
                errBanner.style.display     = "none";
                successBanner.style.display = "block";
                applyForm.style.display     = "none";
                successBanner.scrollIntoView({ behavior: "smooth", block: "nearest" });
                setTimeout(() => { window.location.href = "my-applications.html"; }, 2000);
            } else {
                const msg = await res.text();
                errBanner.textContent   = msg || "Unable to submit your application. Please try again.";
                errBanner.style.display = "block";
                if (submitBtn) { submitBtn.disabled = false; submitBtn.innerHTML = '<i class="fa-solid fa-paper-plane"></i> Submit Application'; }
            }
        } catch (err) {
            console.error(err);
            const errBanner = document.getElementById("errorBanner");
            errBanner.textContent   = "Unable to connect to the server. Please try again.";
            errBanner.style.display = "block";
            if (submitBtn) { submitBtn.disabled = false; submitBtn.innerHTML = '<i class="fa-solid fa-paper-plane"></i> Submit Application'; }
        }
    });
}

// ─── My Applications (Seeker) ─────────────────────────────────────────────────
function createApplicationCard(application) {
    const card = document.createElement("article");
    card.className = "card application-card";

    const title = document.createElement("strong");
    title.textContent = application.jobTitle || `Job #${application.jobId}`;
    card.appendChild(title);

    const info = document.createElement("p");
    info.textContent = `Applied on ${new Date(application.appliedAt).toLocaleDateString("en-IN", { day:"numeric", month:"long", year:"numeric" })}`;
    card.appendChild(info);

    if (application.degree || application.college) {
        const edu = document.createElement("p");
        edu.textContent = [application.degree, application.college].filter(Boolean).join(" · ");
        card.appendChild(edu);
    }

    if (application.coverLetter) {
        const cover = document.createElement("p");
        cover.style.cssText = "font-style:italic;color:#475569";
        cover.textContent = application.coverLetter;
        card.appendChild(cover);
    }

    const status = document.createElement("span");
    status.className = "application-status";
    status.innerHTML = `<i class="fa-solid fa-circle-check"></i> ${application.status || "APPLIED"}`;
    card.appendChild(status);
    return card;
}

async function loadSeekerApplications() {
    const auth = requireAuth("JOB_SEEKER");
    if (!auth) return;
    const res          = await fetch(`${apiBase}/api/applications?seekerEmail=${encodeURIComponent(auth.email)}`);
    const applications = await res.json();
    const list         = document.getElementById("applicationsList");
    list.innerHTML     = "";

    if (applications.length === 0) {
        list.innerHTML = `
            <p style="color:#64748b;padding:40px 0;text-align:center">
                No applications yet. <a href="jobseeker-dashboard.html" style="color:#2563eb;font-weight:600">Browse open jobs →</a>
            </p>`;
        return;
    }
    applications.forEach(a => list.appendChild(createApplicationCard(a)));
}

// ─── Employer: View Applicants ────────────────────────────────────────────────
async function loadEmployerApplicants() {
    const auth   = requireAuth("EMPLOYER");
    if (!auth) return;

    const params = new URLSearchParams(window.location.search);
    const jobId  = params.get("jobId");
    if (!jobId) {
        alert("Invalid job selected.");
        window.location.href = "employer-dashboard.html";
        return;
    }

    // Update page title with job info
    try {
        const jobRes = await fetch(`${apiBase}/api/jobs/${jobId}`);
        if (jobRes.ok) {
            const job = await jobRes.json();
            const pageTitle    = document.getElementById("pageTitle");
            const pageSubtitle = document.getElementById("pageSubtitle");
            if (pageTitle) pageTitle.textContent = `Applicants for: ${job.title}`;
            if (pageSubtitle) pageSubtitle.textContent = `${job.company} · ${job.location}`;
        }
    } catch (_) { /* non-critical */ }

    const res          = await fetch(`${apiBase}/api/applications?jobId=${encodeURIComponent(jobId)}`);
    const applications = await res.json();
    const list         = document.getElementById("applicationsList");
    list.innerHTML     = "";

    if (applications.length === 0) {
        list.innerHTML = `
            <div class="empty-state">
                <i class="fa-solid fa-users"></i>
                <p>No applications have been submitted for this job yet.</p>
            </div>`;
        return;
    }

    applications.forEach(app => {
        const card = document.createElement("article");
        card.className = "applicant-card";

        // Header: avatar + name/email
        const header = document.createElement("div");
        header.className = "applicant-header";
        const avatar = document.createElement("div");
        avatar.className = "applicant-avatar";
        avatar.textContent = (app.seekerName || "?")[0].toUpperCase();
        const nameBlock = document.createElement("div");
        const nameEl  = document.createElement("div");
        nameEl.className = "applicant-name";
        nameEl.textContent = app.seekerName || "Unknown";
        const emailEl = document.createElement("div");
        emailEl.className = "applicant-email";
        emailEl.textContent = app.seekerEmail || "";
        nameBlock.appendChild(nameEl);
        nameBlock.appendChild(emailEl);
        header.appendChild(avatar);
        header.appendChild(nameBlock);
        card.appendChild(header);

        // Detail grid
        const grid = document.createElement("div");
        grid.className = "detail-grid";
        [
            { label: "Age",     value: app.age },
            { label: "Degree",  value: app.degree },
            { label: "College", value: app.college },
        ].forEach(({ label, value }) => {
            if (value) {
                grid.innerHTML += `
                    <div class="detail-item">
                        <span class="detail-label">${label}</span>
                        <span class="detail-value">${value}</span>
                    </div>`;
            }
        });
        if (grid.innerHTML) card.appendChild(grid);

        // Cover letter
        if (app.coverLetter) {
            const note = document.createElement("div");
            note.className = "applicant-note";
            note.innerHTML = `<strong>Cover Letter</strong>${app.coverLetter}`;
            card.appendChild(note);
        }

        // Recommendations
        if (app.recommendations) {
            const rec = document.createElement("div");
            rec.className = "applicant-note";
            rec.innerHTML = `<strong>Recommendations</strong>${app.recommendations}`;
            card.appendChild(rec);
        }

        // Footer: date + status + resume link
        const footer = document.createElement("div");
        footer.className = "applicant-footer";

        const dateEl = document.createElement("span");
        dateEl.className = "applied-date";
        dateEl.innerHTML = `<i class="fa-solid fa-calendar"></i> Applied ${new Date(app.appliedAt).toLocaleDateString("en-IN", { day:"numeric", month:"long", year:"numeric" })}`;
        footer.appendChild(dateEl);

        const rightGroup = document.createElement("div");
        rightGroup.style.cssText = "display:flex;align-items:center;gap:10px;flex-wrap:wrap";

        if (app.resumeFileName) {
            const resumeLink = document.createElement("a");
            resumeLink.className = "resume-link";
            resumeLink.href = `/api/applications/${app.id}/resume`;
            resumeLink.target = "_blank";
            resumeLink.innerHTML = `<i class="fa-solid fa-file-arrow-down"></i> Download Resume`;
            rightGroup.appendChild(resumeLink);
        }

        const statusBadge = document.createElement("span");
        statusBadge.className = "status-badge";
        statusBadge.innerHTML = `<i class="fa-solid fa-circle-check"></i> ${app.status || "APPLIED"}`;
        rightGroup.appendChild(statusBadge);

        footer.appendChild(rightGroup);
        card.appendChild(footer);
        list.appendChild(card);
    });
}

// ─── Bootstrap ───────────────────────────────────────────────────────────────
(async function bootstrap() {
    if (document.getElementById("jobList")) {
        await renderJobSeekerDashboard();
    }
    if (document.getElementById("postedJobsList")) {
        await loadEmployerJobs();
    }
    if (document.getElementById("jobDetails")) {
        await loadJobDetails();
    }
    // Use URL check that works regardless of server path config
    const path = window.location.pathname;
    if (document.getElementById("applicationsList")) {
        if (path.includes("my-applications")) {
            await loadSeekerApplications();
        } else if (path.includes("employer-applications")) {
            await loadEmployerApplicants();
        }
    }
})();
