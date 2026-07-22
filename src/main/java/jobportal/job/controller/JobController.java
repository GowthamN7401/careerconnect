package jobportal.job.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jobportal.job.dto.JobRequest;
import jobportal.job.model.Job;
import jobportal.job.service.JobService;
import jobportal.job.service.UserService;
import jobportal.job.model.User;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    @Autowired
    private JobService jobService;

    @Autowired
    private UserService userService;

    @GetMapping
    public List<Job> getAllJobs() {
        return jobService.getAllJobs();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Long id) {
        Job job = jobService.getJobById(id);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(job);
    }

    @GetMapping("/employer")
    public List<Job> getJobsByEmployer(@RequestParam("email") String email) {
        return jobService.getJobsByEmployerEmail(email);
    }

    @PostMapping
    public ResponseEntity<?> createJob(@RequestBody JobRequest request) {
        User employer = userService.getUserByEmail(request.getEmployerEmail()).orElse(null);
        if (employer == null) {
            return ResponseEntity.badRequest().body("Employer account not found.");
        }
        if (!employer.getRole().name().equals("EMPLOYER")) {
            return ResponseEntity.badRequest().body("Only employer accounts can post jobs.");
        }

        Job job = new Job();
        job.setTitle(request.getTitle());
        job.setCompany(request.getCompany());
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());
        job.setDepartment(request.getDepartment());
        job.setEmploymentType(request.getEmploymentType());
        job.setDescription(request.getDescription());
        job.setEmployerEmail(request.getEmployerEmail());
        job.setEmployerName(request.getEmployerName());

        Job created = jobService.createJob(job);
        return ResponseEntity.ok(created);
    }
}