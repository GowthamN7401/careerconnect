package jobportal.job.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jobportal.job.model.Job;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByEmployerEmail(String employerEmail);

}