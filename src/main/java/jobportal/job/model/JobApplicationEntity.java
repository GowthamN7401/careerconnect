package jobportal.job.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "job_applications")
public class JobApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long jobId;
    private String jobTitle;
    private Long seekerId;
    private String seekerName;
    private String seekerEmail;

    // New applicant profile fields
    private Integer age;
    private String degree;
    private String college;

    @Column(length = 1000)
    private String recommendations;

    @Column(length = 2000)
    private String coverLetter;

    // Resume stored in DB
    private String resumeFileName;

    @Lob
    @Column(name = "resume_data", columnDefinition = "LONGBLOB")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private byte[] resumeData;

    private LocalDateTime appliedAt;
    private String status;

    public JobApplicationEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public Long getSeekerId() { return seekerId; }
    public void setSeekerId(Long seekerId) { this.seekerId = seekerId; }

    public String getSeekerName() { return seekerName; }
    public void setSeekerName(String seekerName) { this.seekerName = seekerName; }

    public String getSeekerEmail() { return seekerEmail; }
    public void setSeekerEmail(String seekerEmail) { this.seekerEmail = seekerEmail; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }

    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }

    public String getResumeFileName() { return resumeFileName; }
    public void setResumeFileName(String resumeFileName) { this.resumeFileName = resumeFileName; }

    public byte[] getResumeData() { return resumeData; }
    public void setResumeData(byte[] resumeData) { this.resumeData = resumeData; }

    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
