package api.userservice;

import api.userservice.model.dto.*;
import api.userservice.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.springframework.test.util.AssertionErrors.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;

import api.userservice.controller.UserController;
import api.userservice.model.Role;
import api.userservice.model.User;
import api.userservice.repository.UserRepository;
import api.userservice.repository.RoleRepository;
import api.userservice.security.JwtUtils;

import java.util.*;

// @ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension.class)
@Import(SecurityConfig.class)
public class UserControllerHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    public void whenRegisterWithValidData_thenRegisterSuccessfully() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("newUser");
        registerDTO.setPassword("Password1.");
        registerDTO.setEmail("newuser@example.com");
        String jsonRegisterDTO = objectMapper.writeValueAsString(registerDTO);

        when(userRepository.existsByUsername(registerDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerDTO.getEmail())).thenReturn(false);
        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        mockMvc.perform(post("/users/register")
                        .content(jsonRegisterDTO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("User registered successfully")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void whenGetAllUsersWithAdminRole_thenListOfUsersReturnedSuccessfully() throws Exception {
        List<User> users = new ArrayList<>();
        User user1 = new User();
        User user2 = new User();
        users.add(user1);
        users.add(user2);

        when(userRepository.findAll()).thenReturn(users);

        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(users.size())));
    }

    @Test
    public void whenLoginWithValidCredentials_thenLoginSuccessful() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("existingUser");
        loginDTO.setPassword("Password1.");
        String jsonLoginDTO = objectMapper.writeValueAsString(loginDTO);

        User foundUser = new User();
        foundUser.setUsername("existingUser");
        foundUser.setPassword("hashedPassword");
        when(userRepository.findByUsername(loginDTO.getUsername())).thenReturn(Optional.of(foundUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/users/login")
                        .content(jsonLoginDTO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Login successful")));
    }

    @Test
    @WithMockUser(username = "1")
    public void whenLogoutWithActiveSession_thenLogoutSuccessful() throws Exception {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setRefreshToken("someRefreshToken");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        mockMvc.perform(post("/users/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Logout successful")));

        // Assert
        verify(userRepository).save(user);
    }



    @Test
    @WithMockUser(username = "1")
    public void whenDeleteUserWithValidId_thenUserDeletedSuccessfully() throws Exception {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(delete("/users/delete")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User deleted successfully")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void whenDeleteByAdminWithValidUsername_thenUserDeletedSuccessfully() throws Exception {
        String username = "existingUser";
        User user = new User();
        user.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        mockMvc.perform(delete("/users/admin/delete/{username}", username)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User deleted successfully")));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    public void whenAddRoleToUserWithValidData_thenRoleAddedSuccessfully() throws Exception {
        RoleToUserDTO roleToUserDTO = new RoleToUserDTO();
        roleToUserDTO.setUsername("existingUser");
        roleToUserDTO.setRoleName("USER");
        String jsonRoleToUserDTO = objectMapper.writeValueAsString(roleToUserDTO);

        User user = new User();
        user.setUsername("existingUser");
        user.setRoles(new HashSet<>()); // Initialize the roles set
        Role role = new Role();
        role.setName("ROLE_USER");
        when(userRepository.findByUsername(roleToUserDTO.getUsername())).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));

        mockMvc.perform(put("/users/admin/addRole")
                        .content(jsonRoleToUserDTO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Role added to user successfully")));
    }

    @Test
    @WithMockUser(username = "1")
    public void whenChangePasswordWithValidData_thenPasswordChangedSuccessfully() throws Exception {
        Long userId = 1L;
        PasswordChangeDTO passwordChangeDTO = new PasswordChangeDTO();
        passwordChangeDTO.setOldPassword("OldPassword1.");
        passwordChangeDTO.setNewPassword("NewPassword1.");
        String jsonPasswordChangeDTO = objectMapper.writeValueAsString(passwordChangeDTO);

        User user = new User();
        user.setId(userId);
        user.setPassword("hashedOldPassword");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(put("/users/changePassword")
                        .content(jsonPasswordChangeDTO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Password changed successfully")));
    }


    @Test
    @WithMockUser(username = "1")
    public void whenChangeEmailWithValidData_thenEmailChangedSuccessfully() throws Exception {
        Long userId = 1L;
        EmailChangeDTO emailChangeDTO = new EmailChangeDTO();
        emailChangeDTO.setNewEmail("newemail@example.com");
        String jsonEmailChangeDTO = objectMapper.writeValueAsString(emailChangeDTO);

        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(emailChangeDTO.getNewEmail())).thenReturn(false);

        mockMvc.perform(put("/users/changeEmail")
                        .content(jsonEmailChangeDTO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Email changed successfully")));
    }


    @Test
    @WithMockUser(username = "1")
    public void whenGetActiveUserInfoWithAuthenticatedUser_thenUserInfoReturnedSuccessfully() throws Exception {
        Long userId = 1L;
        User expectedUser = new User();
        expectedUser.setId(userId);
        expectedUser.setUsername("user");
        expectedUser.setEmail("user@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        mockMvc.perform(get("/users/activeUser")
                        .with(user(new org.springframework.security.core.userdetails.User(userId.toString(), "",
                                new ArrayList<>())))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedUser.getId()))
                .andExpect(jsonPath("$.username").value(expectedUser.getUsername()))
                .andExpect(jsonPath("$.email").value(expectedUser.getEmail()));
    }

}
