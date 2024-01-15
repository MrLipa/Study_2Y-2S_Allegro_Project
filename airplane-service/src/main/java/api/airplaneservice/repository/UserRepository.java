package api.airplaneservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import api.airplaneservice.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);
    
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}