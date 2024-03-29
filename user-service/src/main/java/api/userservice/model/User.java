package api.userservice.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = true)
    private String password;

    @Column(nullable = true, unique = true)
    private String email;

    @Column(nullable = true)
    private String passwordSalt;

    @Column(nullable = true)
    private String refreshToken;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    public User() {
    }

    public User(Long id, String username, String password, String email, String passwordSalt, String refreshToken, Set<Role> roles) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.passwordSalt = passwordSalt;
        this.refreshToken = refreshToken;
        this.roles = roles;
    }

    public User(String id, String password, Collection<Role> roles) {
        this.id = Long.parseLong(id);
        this.password = password;
        this.roles = new HashSet<>(roles);
    }
}
