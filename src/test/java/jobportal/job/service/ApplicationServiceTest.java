package jobportal.job.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jobportal.job.model.Job;
import jobportal.job.model.JobApplicationEntity;
import jobportal.job.repository.JobApplicationRepository;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private JobApplicationRepository applicationRepository;

    @Mock
    private JobService jobService;

    @InjectMocks
    private ApplicationService applicationService;

    private JobApplicationEntity sampleApplication;

    @BeforeEach
    void setUp() {
        sampleApplication = new JobApplicationEntity();
        sampleApplication.setJobId(1L);
        sampleApplication.setSeekerEmail("test@example.com");
        sampleApplication.setSeekerName("Test User");
    }

    // Task 6.1 — hasAlreadyApplied returns true when matching record exists
    @Test
    void hasAlreadyApplied_returnsTrueWhenMatchingRecordExists() {
        when(applicationRepository.findByJobId(1L)).thenReturn(List.of(sampleApplication));

        boolean result = applicationService.hasAlreadyApplied(1L, "test@example.com");

        assertThat(result).isTrue();
    }

    // Task 6.2 — hasAlreadyApplied returns false when no matching record exists
    @Test
    void hasAlreadyApplied_returnsFalseWhenNoMatchingRecord() {
        when(applicationRepository.findByJobId(1L)).thenReturn(List.of());

        boolean result = applicationService.hasAlreadyApplied(1L, "other@example.com");

        assertThat(result).isFalse();
    }

    // Task 6.3 — applyToJob sets status to "APPLIED" and appliedAt to non-null
    @Test
    void applyToJob_setsStatusAndTimestamp() {
        when(jobService.getJobById(1L)).thenReturn(null);
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JobApplicationEntity saved = applicationService.applyToJob(sampleApplication);

        assertThat(saved.getStatus()).isEqualTo("APPLIED");
        assertThat(saved.getAppliedAt()).isNotNull();
    }

    // Task 6.4 — applyToJob copies job title from the resolved Job onto the entity
    @Test
    void applyToJob_copiesJobTitleFromResolvedJob() {
        Job job = new Job();
        job.setTitle("Senior Developer");
        when(jobService.getJobById(1L)).thenReturn(job);
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JobApplicationEntity saved = applicationService.applyToJob(sampleApplication);

        assertThat(saved.getJobTitle()).isEqualTo("Senior Developer");
    }
}
