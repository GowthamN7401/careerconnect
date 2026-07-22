package jobportal.job.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import jobportal.job.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}