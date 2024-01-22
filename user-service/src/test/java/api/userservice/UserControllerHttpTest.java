package api.userservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
import api.userservice.model.dto.LoginDTO;
import api.userservice.model.dto.RegisterDTO;
import api.userservice.repository.UserRepository;
import api.userservice.repository.RoleRepository;
import api.userservice.security.JwtUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
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
        // Arrange
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
    
        // Act & Assert
        mockMvc.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRegisterDTO))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("User registered successfully")));
    }
    
    

    @Test
    @WithMockUser(roles = "ADMIN")
    public void whenGetAllUsersWithAdminRole_thenListOfUsersReturnedSuccessfully() throws Exception {
        // Arrange
        List<User> users = new ArrayList<>();
        User user1 = new User();
        User user2 = new User();
        users.add(user1);
        users.add(user2);
    
        when(userRepository.findAll()).thenReturn(users);
    
        // Act & Assert
        mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(users.size())));
    }
    

    @Test
    public void whenLoginWithValidCredentials_thenLoginSuccessfully() throws Exception {
        // Arrange
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("existingUser");
        loginDTO.setPassword("correctPassword");
        String jsonLoginDTO = objectMapper.writeValueAsString(loginDTO);
        
        User foundUser = new User();
        foundUser.setId(1L);
        foundUser.setUsername(loginDTO.getUsername());
        foundUser.setEmail("user@example.com");
        foundUser.setPassword("hashedPassword");
        foundUser.setPasswordSalt("salt");
        
        Set<Role> roles = new HashSet<>();
        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        roles.add(userRole);
        foundUser.setRoles(roles);

        when(userRepository.findByUsername(loginDTO.getUsername()))
            .thenReturn(Optional.of(foundUser));

        when(passwordEncoder.matches(loginDTO.getPassword() + foundUser.getPasswordSalt(), foundUser.getPassword())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonLoginDTO))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Login successful")));
    }

    @WithMockUser
    @Test
    public void whenGetActiveUserInfoWithAuthenticatedUser_thenUserInfoReturnedSuccessfully() throws Exception {
        // Arrange
        Long userId = 1L;
        User expectedUser = new User();
        expectedUser.setId(userId);
        expectedUser.setUsername("user");
        expectedUser.setEmail("user@example.com");

        given(userRepository.findById(userId)).willReturn(Optional.of(expectedUser));

        // Act & Assert
        mockMvc.perform(get("/users/activeUser")
                .with(user(new org.springframework.security.core.userdetails.User(userId.toString(), "",
                        new ArrayList<>())))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedUser.getId()))
                .andExpect(jsonPath("$.username").value(expectedUser.getUsername()))
                .andExpect(jsonPath("$.email").value(expectedUser.getEmail()));
    }

    // Add more tests here...

}
