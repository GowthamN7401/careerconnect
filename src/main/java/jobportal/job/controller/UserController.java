package jobportal.job.controller;

import java.io.IOException;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jobportal.job.dto.LoginResponse;
import jobportal.job.enums.Role;
import jobportal.job.model.User;
import jobportal.job.service.UserService;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    // Register
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String registerUser(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("role") String role,
            @RequestPart(value = "resume", required = false) MultipartFile resume) throws IOException {

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        Role selectedRole;
        try {
            selectedRole = Role.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return "Invalid role selected.";
        }
        user.setRole(selectedRole);

        if (resume != null && !resume.isEmpty()) {
            user.setResumeFileName(resume.getOriginalFilename());
            user.setResumeData(resume.getBytes());
        } else if (selectedRole == Role.JOB_SEEKER) {
            return "Please upload your resume to complete registration.";
        }

        return userService.registerUser(user);
    }

    // Login
    @PostMapping("/login")
    public LoginResponse loginUser(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("role") String role) {

        Role selectedRole;
        try {
            selectedRole = Role.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return new LoginResponse(false, "Invalid role selected.", null, null, null);
        }

        return userService.loginUser(email, password, selectedRole);
    }
}