package api.userservice.controller;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import api.userservice.model.Role;
import api.userservice.model.User;
import api.userservice.model.dto.EmailChangeDTO;
import api.userservice.model.dto.LoginDTO;
import api.userservice.model.dto.PasswordChangeDTO;
import api.userservice.model.dto.RegisterDTO;
import api.userservice.model.dto.RoleToUserDTO;
import api.userservice.repository.RoleRepository;
import api.userservice.repository.UserRepository;
import api.userservice.security.JwtAuthenticationFilter;
import api.userservice.security.JwtUtils;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private Validator validator;

    UserController() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private <T> ResponseEntity<String> buildValidationErrorsMessage(Set<ConstraintViolation<T>> violations) {
        StringBuilder sb = new StringBuilder();
        for (ConstraintViolation<T> violation : violations) {
            sb.append(violation.getMessage()).append("\n");
        }
        return new ResponseEntity<>(sb.toString(), HttpStatus.BAD_REQUEST);
    }

    final String UserRegistered = "User registered successfully";
    final String UserArleadyExists = "User already exists";

    @Operation(summary = "Register a new user", description = "Register a new user with a username, password, and email. Checks if a user with the same username or email already exists.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = UserRegistered, content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "400", description = "Invalid email or password format", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "409", description = "User already exists", content = @Content(mediaType = "text/plain"))
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDTO registerDTO) {
        String username = registerDTO.getUsername();
        String password = registerDTO.getPassword();
        String email = registerDTO.getEmail();

        Set<ConstraintViolation<RegisterDTO>> violations = validator.validate(registerDTO);
        if (!violations.isEmpty()) {
            return buildValidationErrorsMessage(violations);
        }

        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            return new ResponseEntity<>("User already exists", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);

        String salt = BCrypt.gensalt();
        String hashedPassword = passwordEncoder.encode(password + salt);

        user.setPassword(hashedPassword);
        user.setPasswordSalt(salt);

        Optional<Role> roleOptional = roleRepository.findByName("ROLE_USER");
        if (!roleOptional.isPresent()) {
            return new ResponseEntity<>("Default role not found", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        user.setRoles(Set.of(roleOptional.get()));

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(UserRegistered);
    }

    @Operation(summary = "Get all registered users", description = "Retrieve a list of all users registered in the system. Only accessible by users with admin privileges.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of all users", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User[].class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User is not an admin", content = @Content(mediaType = "text/plain"))
    })
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Operation(summary = "Authenticate a user", description = "Authenticate a user by their username and password using request parameters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Incorrect credentials or use OAuth login", content = @Content(mediaType = "text/plain"))
    })
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO, HttpServletResponse response) {
        Optional<User> foundUser = userRepository.findByUsername(loginDTO.getUsername());
        if (!foundUser.isPresent()) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        User foundUserEntity = foundUser.get();

        if (foundUserEntity.getPassword() == null) {
            return new ResponseEntity<>("No password set for the user. Please login using GitHub.", HttpStatus.UNAUTHORIZED);
        }

        if (passwordEncoder.matches(loginDTO.getPassword() + foundUserEntity.getPasswordSalt(),
                foundUserEntity.getPassword())) {
            String accessToken = jwtUtils.generateAccessToken(foundUserEntity);
            String refreshToken = jwtUtils.generateRefreshToken(foundUserEntity);

            foundUserEntity.setRefreshToken(refreshToken);
            userRepository.save(foundUserEntity);

            JwtAuthenticationFilter.setTokenCookie(response, "accessToken", accessToken,
                    jwtUtils.getJwtExpirationMs() / 1000);
            JwtAuthenticationFilter.setTokenCookie(response, "refreshToken", refreshToken,
                    jwtUtils.getJwtRefreshExpirationMs() / 1000);

            return ResponseEntity.ok("Login successful");
        } else {
            return new ResponseEntity<>("Unauthorized - Incorrect credentials", HttpStatus.UNAUTHORIZED);
        }
    }

    @Operation(summary = "Logout a user", description = "Log out a user using the provided authentication context. This method invalidates the user's session and clears the refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "401", description = "Unauthorized - No active user session or invalid authentication context", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "text/plain"))
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>("No active user session", HttpStatus.UNAUTHORIZED);
        }

        Long userId = Long.parseLong(((UserDetails) authentication.getPrincipal()).getUsername());

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setRefreshToken(null);
            userRepository.save(user);
        } else {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        // Clear cookies
        JwtAuthenticationFilter.clearTokenCookie(response, "accessToken");
        JwtAuthenticationFilter.clearTokenCookie(response, "refreshToken");

        return ResponseEntity.ok("Logout successful");
    }

    @Operation(summary = "Delete a user", description = "Deletes a user based on the ID extracted from the authentication context. Only the user themselves or an admin can delete a user account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "401", description = "Unauthorized - No active user session or invalid authentication context", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "text/plain"))
    })

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>("No active user session", HttpStatus.UNAUTHORIZED);
        }

        Long userId = Long.parseLong(((UserDetails) authentication.getPrincipal()).getUsername());

        // Perform the user deletion
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        userRepository.deleteById(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    @Operation(summary = "Delete a user by admin", description = "Allows an admin to delete a user by their username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Only accessible by admins", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "text/plain"))
    })
    @DeleteMapping("/admin/delete/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteByAdmin(@PathVariable String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (!userOptional.isPresent()) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        userRepository.delete(userOptional.get());
        return ResponseEntity.ok("User deleted successfully");
    }

    @Operation(summary = "Add role to user", description = "Adds a specified role to a user with a given username after verifying that both the user and the role exist.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role added to user successfully", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "404", description = "User or role not found", content = @Content(mediaType = "text/plain"))
    })

    @PutMapping("/admin/addRole")
    public ResponseEntity<String> addRoleToUser(@RequestBody RoleToUserDTO roleToUserDTO) {
        String roleName = "ROLE_" + roleToUserDTO.getRoleName().toUpperCase();

        Optional<Role> role = roleRepository.findByName(roleName);
        if (!role.isPresent()) {
            return new ResponseEntity<>("Role not found", HttpStatus.NOT_FOUND);
        }

        Optional<User> user = userRepository.findByUsername(roleToUserDTO.getUsername());
        if (!user.isPresent()) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        user.get().getRoles().add(role.get());
        userRepository.save(user.get());

        return ResponseEntity.ok("Role added to user successfully");
    }

    @Operation(summary = "Change User Password", description = "Allows a user to change their password. The user is identified by their authentication context, and the request must include the old and new password.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "400", description = "Invalid old password or new password format", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "401", description = "Unauthorized - No active user session or invalid authentication context", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "text/plain"))
    })

    @PutMapping("/changePassword")
    public ResponseEntity<String> changePassword(Authentication authentication,
            @RequestBody PasswordChangeDTO passwordChangeDTO) {

        Set<ConstraintViolation<PasswordChangeDTO>> violations = validator.validate(passwordChangeDTO);
        if (!violations.isEmpty()) {
            return buildValidationErrorsMessage(violations);
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>("No active user session", HttpStatus.UNAUTHORIZED);
        }

        Long userId = Long.parseLong(((UserDetails) authentication.getPrincipal()).getUsername());

        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(passwordChangeDTO.getOldPassword() + user.getPasswordSalt(), user.getPassword())) {
            return new ResponseEntity<>("Invalid old password", HttpStatus.BAD_REQUEST);
        }

        String salt = BCrypt.gensalt();
        String hashedNewPassword = passwordEncoder.encode(passwordChangeDTO.getNewPassword() + salt);
        user.setPassword(hashedNewPassword);
        user.setPasswordSalt(salt);

        userRepository.save(user);

        return ResponseEntity.ok("Password changed successfully");
    }

    @Operation(summary = "Change User Email", description = "Allows a user to change their email address. The user is identified by their authentication context, and the request must include the new email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email changed successfully", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid email format or email already in use", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "401", description = "Unauthorized - No active user session or invalid authentication context", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "text/plain"))
    })

    @PutMapping("/changeEmail")
    public ResponseEntity<String> changeEmail(Authentication authentication,
            @RequestBody EmailChangeDTO emailChangeDTO) {
        Set<ConstraintViolation<EmailChangeDTO>> violations = validator.validate(emailChangeDTO);
        if (!violations.isEmpty()) {
            return buildValidationErrorsMessage(violations);
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>("No active user session", HttpStatus.UNAUTHORIZED);
        }

        Long userId = Long.parseLong(((UserDetails) authentication.getPrincipal()).getUsername());

        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        User user = userOptional.get();
        String newEmail = emailChangeDTO.getNewEmail();

        if (userRepository.existsByEmail(newEmail)) {
            return new ResponseEntity<>("Email already in use", HttpStatus.BAD_REQUEST);
        }

        user.setEmail(newEmail);
        userRepository.save(user);

        return ResponseEntity.ok("Email changed successfully");
    }

    @Operation(summary = "Get Active User Info", description = "Retrieve information about the currently authenticated user.")
    @ApiResponse(responseCode = "200", description = "User information retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/activeUser")
    public ResponseEntity<User> getActiveUserInfo(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Long userId = Long.parseLong(((UserDetails) authentication.getPrincipal()).getUsername());

        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        User user = userOptional.get();
        return ResponseEntity.ok(user);
    }
}
