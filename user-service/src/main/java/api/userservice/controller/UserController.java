package api.userservice.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import api.userservice.model.Role;
import api.userservice.model.User;
import api.userservice.repository.RoleRepository;
import api.userservice.repository.UserRepository;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    final String UserRegistered = "User registered successfully";

    @Operation(summary = "Register a new user", 
        description = "Register a new user with a username, password, and email",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class),
                examples = @ExampleObject(
                    value = "{\"username\": \"johndoe\", \"password\": \"Password123\", \"email\": \"johndoe@example.com\"}"
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = UserRegistered,
            content = @Content(mediaType = "text/plain")),
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        user.setId(null);
        user.setPasswordSalt(null);
        
        String salt = BCrypt.gensalt();
        String hashedPassword = passwordEncoder.encode(user.getPassword() + salt); 
        
        user.setPassword(hashedPassword); 
        user.setPasswordSalt(salt);

        Role userRole = roleRepository.findByName("ROLE_USER");
        user.setRoles(Set.of(userRole));
        
        userRepository.save(user);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(UserRegistered);
    }


    @Profile("development")
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // public User getUserByUsername(String username) {
    // return userRepository.findByUsername(username)
    //                      .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    // }
    

    // @PostMapping("/login")
    // public ResponseEntity<User> login(@RequestBody User user) {
    //     Optional<User> foundUser = userRepository.findByUsername(user.getUsername());
    //     if (foundUser == null) {
    //         return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    //     }

    //     String hashedPassword = passwordEncoder.encode(user.getPassword() + foundUser.getPasswordSalt());
    //     if (hashedPassword.equals(foundUser.getPassword())) {
    //         return new ResponseEntity<>(foundUser, HttpStatus.OK);
    //     } else {
    //         return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    //     }
    // }
}
