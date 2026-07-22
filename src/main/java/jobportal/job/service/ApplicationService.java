package jobportal.job.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jobportal.job.model.Job;
import jobportal.job.model.JobApplicationEntity;
import jobportal.job.repository.JobApplicationRepository;

@Service
public class ApplicationService {

    @Autowired
    private JobApplicationRepository applicationRepository;

    @Autowired
    private JobService jobService;

    public JobApplicationEntity applyToJob(JobApplicationEntity application) {
        Job job = jobService.getJobById(application.getJobId());
        if (job != null) {
            application.setJobTitle(job.getTitle());
        }
        application.setAppliedAt(LocalDateTime.now());
        application.setStatus("APPLIED");
        return applicationRepository.save(application);
    }

    public Optional<JobApplicationEntity> getApplicationById(Long id) {
        return applicationRepository.findById(id);
    }

    public List<JobApplicationEntity> getApplicationsBySeekerEmail(String seekerEmail) {
        return applicationRepository.findBySeekerEmail(seekerEmail);
    }

    public List<JobApplicationEntity> getApplicationsByJobId(Long jobId) {
        return applicationRepository.findByJobId(jobId);
    }

    public boolean hasAlreadyApplied(Long jobId, String seekerEmail) {
        return applicationRepository.findByJobId(jobId)
                .stream()
                .anyMatch(application -> application.getSeekerEmail().equalsIgnoreCase(seekerEmail));
    }
}
