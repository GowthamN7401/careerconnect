package jobportal.job.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jobportal.job.model.JobApplicationEntity;

public interface JobApplicationRepository extends JpaRepository<JobApplicationEntity, Long> {

    List<JobApplicationEntity> findBySeekerEmail(String seekerEmail);

    List<JobApplicationEntity> findByJobId(Long jobId);

}