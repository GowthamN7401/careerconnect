package jobportal.job.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jobportal.job.enums.Role;
import jobportal.job.model.JobApplicationEntity;
import jobportal.job.model.User;
import jobportal.job.service.ApplicationService;
import jobportal.job.service.UserService;

@WebMvcTest(ApplicationController.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicationService applicationService;

    @MockitoBean
    private UserService userService;

    // Task 7.1 — POST /api/applications returns HTTP 400 when duplicate application
    @Test
    void applyToJob_returns400WhenAlreadyApplied() throws Exception {
        User seeker = new User();
        seeker.setId(1L);
        seeker.setName("Jane Doe");
        seeker.setEmail("jane@example.com");
        seeker.setPassword("pass");
        seeker.setRole(Role.JOB_SEEKER);

        when(userService.getUserByEmail("jane@example.com")).thenReturn(Optional.of(seeker));
        when(applicationService.hasAlreadyApplied(anyLong(), anyString())).thenReturn(true);

        mockMvc.perform(multipart("/api/applications")
                        .param("jobId", "1")
                        .param("seekerName", "Jane Doe")
                        .param("seekerEmail", "jane@example.com")
                        .param("age", "25")
                        .param("degree", "B.Tech")
                        .param("college", "MIT"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("You have already applied for this job."));
    }

    // Task 7.2 — GET /api/applications/{id}/resume returns HTTP 404 when resumeData is null
    @Test
    void downloadResume_returns404WhenNoResumeData() throws Exception {
        JobApplicationEntity app = new JobApplicationEntity();
        app.setId(1L);
        app.setResumeData(null);

        when(applicationService.getApplicationById(1L)).thenReturn(Optional.of(app));

        mockMvc.perform(get("/api/applications/1/resume"))
                .andExpect(status().isNotFound());
    }

    // Extra: GET /api/applications/{id}/resume returns HTTP 404 when application not found
    @Test
    void downloadResume_returns404WhenApplicationNotFound() throws Exception {
        when(applicationService.getApplicationById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/applications/99/resume"))
                .andExpect(status().isNotFound());
    }
}
