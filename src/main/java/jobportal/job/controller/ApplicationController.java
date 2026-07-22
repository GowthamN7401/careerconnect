package jobportal.job.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jobportal.job.model.JobApplicationEntity;
import jobportal.job.model.User;
import jobportal.job.service.ApplicationService;
import jobportal.job.service.UserService;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private UserService userService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> applyToJob(
            @RequestParam("jobId") Long jobId,
            @RequestParam("seekerName") String seekerName,
            @RequestParam("seekerEmail") String seekerEmail,
            @RequestParam("age") Integer age,
            @RequestParam("degree") String degree,
            @RequestParam("college") String college,
            @RequestParam(value = "recommendations", required = false) String recommendations,
            @RequestParam(value = "coverLetter", required = false) String coverLetter,
            @RequestParam(value = "resume", required = false) MultipartFile resume) throws IOException {

        User user = userService.getUserByEmail(seekerEmail).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("Applicant account not found.");
        }
        if (!user.getRole().name().equals("JOB_SEEKER")) {
            return ResponseEntity.badRequest().body("Only job seekers can apply for jobs.");
        }
        if (applicationService.hasAlreadyApplied(jobId, seekerEmail)) {
            return ResponseEntity.badRequest().body("You have already applied for this job.");
        }

        JobApplicationEntity application = new JobApplicationEntity();
        application.setJobId(jobId);
        application.setSeekerId(user.getId());
        application.setSeekerEmail(seekerEmail);
        application.setSeekerName(seekerName);
        application.setAge(age);
        application.setDegree(degree);
        application.setCollege(college);
        application.setRecommendations(recommendations);
        application.setCoverLetter(coverLetter);

        if (resume != null && !resume.isEmpty()) {
            application.setResumeFileName(resume.getOriginalFilename());
            application.setResumeData(resume.getBytes());
        }

        try {
            JobApplicationEntity saved = applicationService.applyToJob(application);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to save application: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<JobApplicationEntity>> getApplications(
            @RequestParam(value = "jobId", required = false) Long jobId,
            @RequestParam(value = "seekerEmail", required = false) String seekerEmail) {

        if (jobId != null) {
            return ResponseEntity.ok(applicationService.getApplicationsByJobId(jobId));
        }
        if (seekerEmail != null && !seekerEmail.isBlank()) {
            return ResponseEntity.ok(applicationService.getApplicationsBySeekerEmail(seekerEmail));
        }
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}/resume")
    public ResponseEntity<byte[]> downloadResume(@PathVariable Long id) {
        var appOpt = applicationService.getApplicationById(id);
        if (appOpt.isEmpty()) {
            return ResponseEntity.<byte[]>notFound().build();
        }
        JobApplicationEntity app = appOpt.get();
        if (app.getResumeData() == null || app.getResumeData().length == 0) {
            return ResponseEntity.<byte[]>notFound().build();
        }
        String filename = app.getResumeFileName() != null ? app.getResumeFileName() : "resume.pdf";
        MediaType mediaType = filename.endsWith(".pdf") ? MediaType.APPLICATION_PDF
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.<byte[]>ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(app.getResumeData());
    }
}
