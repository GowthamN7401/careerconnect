package jobportal.job.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jobportal.job.dto.LoginResponse;
import jobportal.job.enums.Role;
import jobportal.job.model.User;
import jobportal.job.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Register User
    public String registerUser(User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            return "Email already registered!";
        }

        userRepository.save(user);
        return "Registration Successful";
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Login User
    public LoginResponse loginUser(String email, String password, Role role) {

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return new LoginResponse(false,
                    "Account not found. Please register first.",
                    null, null, null);
        }

        User user = optionalUser.get();

        if (!user.getPassword().equals(password)) {
            return new LoginResponse(false,
                    "Incorrect password.",
                    null, null, null);
        }

        if (user.getRole() != role) {
            return new LoginResponse(false,
                    "The selected role does not match this account.",
                    null, null, null);
        }

        return new LoginResponse(
                true,
                "Login Successful",
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}