package api.userservice;

import api.userservice.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import api.userservice.model.User;
import api.userservice.controller.UserController;
import api.userservice.model.Role;
import api.userservice.model.dto.EmailChangeDTO;
import api.userservice.model.dto.LoginDTO;
import api.userservice.model.dto.PasswordChangeDTO;
import api.userservice.model.dto.RegisterDTO;
import api.userservice.model.dto.RoleToUserDTO;
import api.userservice.repository.UserRepository;
import api.userservice.repository.RoleRepository;
import api.userservice.security.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private Validator validator;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setup() {
    }

    private RegisterDTO createRegisterDTO(String username, String password, String email) {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername(username);
        registerDTO.setPassword(password);
        registerDTO.setEmail(email);
        return registerDTO;
    }

    @Test
    public void whenRegisterWithNewUsernameAndEmail_thenRegisterSuccessfully() {
        RegisterDTO registerDTO = createRegisterDTO("newUser", "password", "newuser@example.com");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(roleRepository.findByName(anyString())).thenReturn(Optional.of(new Role()));

        ResponseEntity<String> response = userController.register(registerDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("User registered successfully", response.getBody());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void whenRegisterWithExistingUsername_thenConflict() {
        RegisterDTO registerDTO = createRegisterDTO("existingUser", "password", "user@example.com");
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        ResponseEntity<String> response = userController.register(registerDTO);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("User already exists", response.getBody());
    }

    @Test
    public void whenRegisterWithExistingEmail_thenConflict() {
        RegisterDTO registerDTO = createRegisterDTO("newUser", "password", "existing@example.com");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        ResponseEntity<String> response = userController.register(registerDTO);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("User already exists", response.getBody());
    }

    private <T> ConstraintViolation<T> mockConstraintViolation(String message) {
        ConstraintViolation<T> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }

    @Test
    public void whenRegisterWithBlankUsername_thenBadRequest() {
        RegisterDTO registerDTO = createRegisterDTO("", "Password1", "user@example.com");
        Set<ConstraintViolation<RegisterDTO>> violations = new HashSet<>();
        violations.add(mockConstraintViolation("Username is required"));
        when(validator.validate(registerDTO)).thenReturn(violations);

        ResponseEntity<String> response = userController.register(registerDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Username is required"));
    }

    @Test
    public void whenRegisterWithInvalidPassword_thenBadRequest() {
        RegisterDTO registerDTO = createRegisterDTO("newUser", "pass", "user@example.com");
        Set<ConstraintViolation<RegisterDTO>> violations = new HashSet<>();
        violations.add(mockConstraintViolation(
                "Password must be at least 8 characters long and include at least one number and one uppercase letter"));
        when(validator.validate(registerDTO)).thenReturn(violations);

        ResponseEntity<String> response = userController.register(registerDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains(
                "Password must be at least 8 characters long and include at least one number and one uppercase letter"));
    }

    @Test
    public void whenRegisterWithInvalidEmail_thenBadRequest() {
        RegisterDTO registerDTO = createRegisterDTO("newUser", "Password1", "invalidemail");
        Set<ConstraintViolation<RegisterDTO>> violations = new HashSet<>();
        violations.add(mockConstraintViolation("Email should be valid"));
        when(validator.validate(registerDTO)).thenReturn(violations);

        ResponseEntity<String> response = userController.register(registerDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Email should be valid"));
    }

    private User createUser(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }

    @Test
    public void whenGetAllUsers_thenReturnListOfUsers() {
        // Arrange
        List<User> expectedUsers = new ArrayList<>();
        expectedUsers.add(createUser("user1", "user1@example.com", "password1"));
        expectedUsers.add(createUser("user2", "user2@example.com", "password2"));

        when(userRepository.findAll()).thenReturn(expectedUsers);

        // Act
        List<User> actualUsers = userController.getAllUsers();

        // Assert
        assertEquals(expectedUsers.size(), actualUsers.size(), "The size of expected and actual users should match");
        assertEquals(expectedUsers, actualUsers, "The expected users and actual users lists should match");
        verify(userRepository, times(1)).findAll();
    }

    private LoginDTO createLoginDTO(String username, String password) {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(username);
        loginDTO.setPassword(password);
        return loginDTO;
    }

    @Test
    public void whenLoginWithValidCredentials_thenLoginSuccessfully() {
        LoginDTO loginDTO = createLoginDTO("user", "correctPassword");
        User foundUser = new User();
        foundUser.setUsername("user");
        foundUser.setPassword("hashedPassword");
        foundUser.setPasswordSalt("salt");

        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.of(foundUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtils.generateAccessToken(any(User.class))).thenReturn("accessToken");
        when(jwtUtils.generateRefreshToken(any(User.class))).thenReturn("refreshToken");

        ResponseEntity<String> response = userController.login(loginDTO, mock(HttpServletResponse.class));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Login successful", response.getBody());
    }

    @Test
    public void whenLoginWithNonExistingUsername_thenUserNotFound() {
        LoginDTO loginDTO = createLoginDTO("nonExistingUser", "password");

        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.empty());

        ResponseEntity<String> response = userController.login(loginDTO, mock(HttpServletResponse.class));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody());
    }

    @Test
    public void whenLoginWithInvalidPassword_thenUnauthorized() {
        LoginDTO loginDTO = createLoginDTO("user", "wrongPassword");
        User foundUser = new User();
        foundUser.setUsername("user");
        foundUser.setPassword("hashedPassword");
        foundUser.setPasswordSalt("salt");

        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.of(foundUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        ResponseEntity<String> response = userController.login(loginDTO, mock(HttpServletResponse.class));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthorized - Incorrect credentials", response.getBody());
    }

    @Test
    public void whenLoginWithNoPasswordSet_thenShouldLoginViaOAuth() {
        LoginDTO loginDTO = createLoginDTO("oauthUser", "anyPassword");
        User foundUser = new User();
        foundUser.setUsername("oauthUser");
        foundUser.setPassword(null); // Password not set

        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.of(foundUser));

        ResponseEntity<String> response = userController.login(loginDTO, mock(HttpServletResponse.class));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("No password set for the user. Please login using GitHub.", response.getBody());
    }

    @Test
    public void whenLogoutWithAuthenticatedUser_thenLogoutSuccessfully() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setRefreshToken("someRefreshToken");

        UserDetails mockUserDetails = mock(UserDetails.class);
        when(mockUserDetails.getUsername()).thenReturn(userId.toString());

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(mockUserDetails);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        HttpServletResponse response = mock(HttpServletResponse.class);

        SecurityContextHolder.setContext(securityContext);

        // Act
        ResponseEntity<String> result = userController.logout(response);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Logout successful", result.getBody());
        assertNull(user.getRefreshToken(), "Refresh token should be cleared");
        verify(userRepository).save(user);
        JwtAuthenticationFilter.clearTokenCookie(response, "accessToken");
        JwtAuthenticationFilter.clearTokenCookie(response, "refreshToken");

        // Clear SecurityContextHolder after test
        SecurityContextHolder.clearContext();
    }




    @Test
    public void whenLogoutWithUnauthenticatedUser_thenUnauthorized() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        HttpServletResponse response = mock(HttpServletResponse.class);

        // Set SecurityContextHolder to use the mocked SecurityContext
        SecurityContextHolder.setContext(securityContext);

        // Act
        ResponseEntity<String> result = userController.logout(response);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertEquals("No active user session", result.getBody());

        // Clear SecurityContextHolder after test
        SecurityContextHolder.clearContext();
    }


    @Test
    public void whenDeleteUserWithAuthenticatedUser_thenDeleteSuccessfully() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        UserDetails mockUserDetails = mock(UserDetails.class);
        when(mockUserDetails.getUsername()).thenReturn(userId.toString());

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(mockUserDetails);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        SecurityContextHolder.setContext(securityContext);

        // Act
        ResponseEntity<String> result = userController.deleteUser();

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("User deleted successfully", result.getBody());
        verify(userRepository, times(1)).deleteById(userId);

        // Clear SecurityContextHolder after test
        SecurityContextHolder.clearContext();
    }

    @Test
    public void whenDeleteUserWithUnauthenticatedUser_thenUnauthorized() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Act
        ResponseEntity<String> result = userController.deleteUser();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertEquals("No active user session", result.getBody());

        // Clear SecurityContextHolder after test
        SecurityContextHolder.clearContext();
    }

    @Test
    public void whenDeleteNonExistingUser_thenNotFound() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(
                new org.springframework.security.core.userdetails.User("1", "", new ArrayList<>()));

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Long userId = Long.parseLong(((UserDetails) authentication.getPrincipal()).getUsername());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> result = userController.deleteUser();

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("User not found", result.getBody());

        // Clear SecurityContextHolder after test
        SecurityContextHolder.clearContext();
    }

    @Test
    public void whenAdminDeletesUser_thenDeleteSuccessfully() {
        // Arrange
        String username = "userToDelete";
        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        ResponseEntity<String> result = userController.deleteByAdmin(username);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("User deleted successfully", result.getBody());
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    public void whenAdminDeletesNonExistingUser_thenNotFound() {
        // Arrange
        String username = "nonExistingUser";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> result = userController.deleteByAdmin(username);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("User not found", result.getBody());
    }

    private RoleToUserDTO createRoleToUserDTO(String username, String roleName) {
        RoleToUserDTO roleToUserDTO = new RoleToUserDTO();
        roleToUserDTO.setUsername(username);
        roleToUserDTO.setRoleName(roleName);
        return roleToUserDTO;
    }

    @Test
    public void whenAddRoleToExistingUser_thenRoleAddedSuccessfully() {
        // Arrange
        String username = "existingUser";
        String roleName = "USER";
        String roleType = "ROLE_" + roleName;
        RoleToUserDTO roleToUserDTO = createRoleToUserDTO(username, roleName);
        User user = new User();
        user.setUsername(username);
        user.setRoles(new HashSet<>());
        Role role = new Role();
        role.setName(roleType);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(roleType)).thenReturn(Optional.of(role));

        // Act
        ResponseEntity<String> result = userController.addRoleToUser(roleToUserDTO);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Role added to user successfully", result.getBody());
        assertTrue(user.getRoles().contains(role));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void whenAddNonExistingRoleToUser_thenRoleNotFound() {
        // Arrange
        String username = "existingUser";
        String roleName = "NON_EXISTING_ROLE";
        RoleToUserDTO roleToUserDTO = createRoleToUserDTO(username, roleName);

        // Act
        ResponseEntity<String> result = userController.addRoleToUser(roleToUserDTO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Role not found", result.getBody());
    }

    @Test
    public void whenAddRoleToNonExistingUser_thenUserNotFound() {
        // Arrange
        String username = "nonExistingUser";
        String roleName = "USER";
        RoleToUserDTO roleToUserDTO = createRoleToUserDTO(username, roleName);
        Role role = new Role();
        role.setName("ROLE_" + roleName);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(roleRepository.findByName(role.getName())).thenReturn(Optional.of(role));

        // Act
        ResponseEntity<String> result = userController.addRoleToUser(roleToUserDTO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("User not found", result.getBody());
    }

    @Test
    public void whenChangePasswordWithValidInput_thenPasswordChangedSuccessfully() {
        // Arrange
        PasswordChangeDTO passwordChangeDTO = new PasswordChangeDTO();
        passwordChangeDTO.setOldPassword("oldPassword");
        passwordChangeDTO.setNewPassword("newPassword");

        UserDetails userDetails = new org.springframework.security.core.userdetails.User("1", "", new ArrayList<>());
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContextHolder.setContext(securityContext);

        Long userId = Long.parseLong(userDetails.getUsername());
        User user = new User();
        user.setId(userId);
        user.setPasswordSalt("salt");
        String oldPasswordHash = "hashedOldPassword";
        user.setPassword(oldPasswordHash);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedNewPassword");

        // Act
        ResponseEntity<String> response = userController.changePassword(passwordChangeDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password changed successfully", response.getBody());
        verify(userRepository, times(1)).save(user);

        // Clear SecurityContextHolder after test
        SecurityContextHolder.clearContext();
    }

    @Test
    public void whenChangePasswordWithInvalidOldPassword_thenBadRequest() {
        // Arrange
        PasswordChangeDTO passwordChangeDTO = new PasswordChangeDTO();
        passwordChangeDTO.setOldPassword("invalidOldPassword");
        passwordChangeDTO.setNewPassword("newPassword");

        UserDetails userDetails = new org.springframework.security.core.userdetails.User("1", "", new ArrayList<>());
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContextHolder.setContext(securityContext);

        Long userId = Long.parseLong(userDetails.getUsername());
        User user = new User();
        user.setId(userId);
        user.setPasswordSalt("salt");
        String oldPasswordHash = "hashedOldPassword";
        user.setPassword(oldPasswordHash);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act
        ResponseEntity<String> response = userController.changePassword(passwordChangeDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid old password", response.getBody());
        verify(userRepository, never()).save(user);

        // Clear SecurityContextHolder after test
        SecurityContextHolder.clearContext();
    }

    @Test
    public void whenChangePasswordWithUnauthenticatedUser_thenUnauthorized() {
        // Arrange
        PasswordChangeDTO passwordChangeDTO = new PasswordChangeDTO();
        passwordChangeDTO.setOldPassword("oldPassword");
        passwordChangeDTO.setNewPassword("newPassword");

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Act
        ResponseEntity<String> response = userController.changePassword(passwordChangeDTO);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("No active user session", response.getBody());

        // Clear SecurityContextHolder after test
        SecurityContextHolder.clearContext();
    }

    @Test
    public void whenChangePasswordWithNonExistingUser_thenNotFound() {
        // Arrange
        PasswordChangeDTO passwordChangeDTO = new PasswordChangeDTO();
        passwordChangeDTO.setOldPassword("oldPassword");
        passwordChangeDTO.setNewPassword("newPassword");

        UserDetails userDetails = new org.springframework.security.core.userdetails.User("1", "", new ArrayList<>());
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContextHolder.setContext(securityContext);

        Long userId = Long.parseLong(userDetails.getUsername());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = userController.changePassword(passwordChangeDTO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody());
        verify(userRepository, never()).save(any(User.class));

        // Clear SecurityContextHolder after test
        SecurityContextHolder.clearContext();
    }

    @Test
    public void whenChangeEmailWithValidInput_thenEmailChangedSuccessfully() {
        // Arrange
        EmailChangeDTO emailChangeDTO = new EmailChangeDTO();
        emailChangeDTO.setNewEmail("newemail@example.com");

        UserDetails userDetails = new org.springframework.security.core.userdetails.User("1", "", new ArrayList<>());
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContextHolder.setContext(securityContext);

        Long userId = Long.parseLong(userDetails.getUsername());
        User user = new User();
        user.setId(userId);
        user.setEmail("oldemail@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(emailChangeDTO.getNewEmail())).thenReturn(false);

        // Act
        ResponseEntity<String> response = userController.changeEmail(emailChangeDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Email changed successfully", response.getBody());
        assertEquals(emailChangeDTO.getNewEmail(), user.getEmail());
        verify(userRepository, times(1)).save(user);

        // Clear SecurityContextHolder after test
        SecurityContextHolder.clearContext();
    }

    @Test
    public void whenChangeEmailWithEmailAlreadyInUse_thenBadRequest() {
        // Arrange
        EmailChangeDTO emailChangeDTO = new EmailChangeDTO();
        emailChangeDTO.setNewEmail("newemail@example.com");

        UserDetails userDetails = new org.springframework.security.core.userdetails.User("1", "", new ArrayList<>());
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContextHolder.setContext(securityContext);

        Long userId = Long.parseLong(userDetails.getUsername());
        User user = new User();
        user.setId(userId);
        user.setEmail("oldemail@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(emailChangeDTO.getNewEmail())).thenReturn(true);

        // Act
        ResponseEntity<String> response = userController.changeEmail(emailChangeDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email already in use", response.getBody());

        // Clear SecurityContextHolder after test
        SecurityContextHolder.clearContext();
    }

    @Test
    public void whenChangeEmailWithUnauthenticatedUser_thenUnauthorized() {
        // Arrange
        EmailChangeDTO emailChangeDTO = new EmailChangeDTO();
        emailChangeDTO.setNewEmail("newemail@example.com");

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Act
        ResponseEntity<String> response = userController.changeEmail(emailChangeDTO);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("No active user session", response.getBody());

        // Clear SecurityContextHolder after test
        SecurityContextHolder.clearContext();
    }

    @Test
    public void whenChangeEmailWithNonExistingUser_thenNotFound() {
        // Arrange
        EmailChangeDTO emailChangeDTO = new EmailChangeDTO();
        emailChangeDTO.setNewEmail("newemail@example.com");

        UserDetails userDetails = new org.springframework.security.core.userdetails.User("1", "", new ArrayList<>());
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContextHolder.setContext(securityContext);

        Long userId = Long.parseLong(userDetails.getUsername());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<String> response = userController.changeEmail(emailChangeDTO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody());
        verify(userRepository, never()).save(any(User.class));

        // Clear SecurityContextHolder after test
        SecurityContextHolder.clearContext();
    }

    @Test
    public void whenGetActiveUserInfoWithAuthenticatedUser_thenUserInfoReturnedSuccessfully() {
        // Arrange
        Long userId = 1L;
        User expectedUser = new User();
        expectedUser.setId(userId);
        expectedUser.setUsername("user");
        expectedUser.setEmail("user@example.com");

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(userId.toString(), "", new ArrayList<>());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        // Act
        ResponseEntity<User> response = userController.getActiveUserInfo();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedUser, response.getBody());

        // Clear SecurityContextHolder after test
        SecurityContextHolder.clearContext();
    }

    @Test
    public void whenGetActiveUserInfoWithUnauthenticatedUser_thenUnauthorized() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Act
        ResponseEntity<User> response = userController.getActiveUserInfo();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());

        // Clear SecurityContextHolder after test
        SecurityContextHolder.clearContext();
    }

    @Test
    public void whenGetActiveUserInfoWithNonExistingUser_thenNotFound() {
        // Arrange
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("1", "", new ArrayList<>());
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContextHolder.setContext(securityContext);

        Long userId = Long.parseLong(userDetails.getUsername());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<User> response = userController.getActiveUserInfo();

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        // Clear SecurityContextHolder after test
        SecurityContextHolder.clearContext();
    }
}
