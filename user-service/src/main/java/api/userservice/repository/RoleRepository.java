package api.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import api.userservice.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}
