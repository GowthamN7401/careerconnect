package jobportal.job.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jobportal.job.model.Job;
import jobportal.job.repository.JobRepository;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    public Job createJob(Job job) {
        job.setPostedAt(LocalDateTime.now());
        return jobRepository.save(job);
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public List<Job> getJobsByEmployerEmail(String employerEmail) {
        return jobRepository.findByEmployerEmail(employerEmail);
    }

    public Job getJobById(Long id) {
        return jobRepository.findById(id).orElse(null);
    }
}