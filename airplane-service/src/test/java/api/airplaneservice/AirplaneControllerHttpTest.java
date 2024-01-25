package api.airplaneservice;

import api.airplaneservice.controller.AirplaneController;
import api.airplaneservice.model.Airplane;
import api.airplaneservice.repository.AirplaneRepository;
import api.airplaneservice.repository.UserRepository;
import api.airplaneservice.security.JwtUtils;
import api.airplaneservice.security.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AirplaneController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension.class)
@Import(SecurityConfig.class) // Import your security configuration if needed
public class AirplaneControllerHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AirplaneRepository airplaneRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    public void whenGetAllAirplanes_thenReturnListOfAirplanes() throws Exception {
        // Arrange
        List<Airplane> airplanes = new ArrayList<>();
        airplanes.add(new Airplane(/* ... */));
        airplanes.add(new Airplane(/* ... */));
        when(airplaneRepository.findAll()).thenReturn(airplanes);

        // Act & Assert
        mockMvc.perform(get("/airplanes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(airplanes.size())));
    }

    @Test
    public void whenGetAirplaneByIdWithExistingId_thenReturnAirplane() throws Exception {
        // Arrange
        Long id = 1L;
        Airplane airplane = new Airplane(/* ... */);
        when(airplaneRepository.findById(id)).thenReturn(Optional.of(airplane));

        // Act & Assert
        mockMvc.perform(get("/airplanes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(airplane.getId()));
    }

    @Test
    public void whenGetAirplaneByIdWithNonExistingId_thenNotFound() throws Exception {
        // Arrange
        Long id = 1L;
        when(airplaneRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/airplanes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void whenCreateAirplane_thenSaveAndReturnAirplane() throws Exception {
        // Arrange
        Airplane airplane = new Airplane(/* ... */);
        String jsonAirplane = objectMapper.writeValueAsString(airplane);
        when(airplaneRepository.save(airplane)).thenReturn(airplane);

        // Act & Assert
        mockMvc.perform(post("/airplanes")
                        .content(jsonAirplane)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(airplane.getId()));
    }

    @Test
    public void whenUpdateExistingAirplane_thenUpdateAndReturnAirplane() throws Exception {
        // Arrange
        Long id = 1L;
        Airplane existingAirplane = new Airplane(/* ... */);
        Airplane updatedDetails = new Airplane(/* ... */);
        String jsonUpdatedDetails = objectMapper.writeValueAsString(updatedDetails);
        when(airplaneRepository.findById(id)).thenReturn(Optional.of(existingAirplane));
        when(airplaneRepository.save(any(Airplane.class))).thenReturn(updatedDetails);

        // Act & Assert
        mockMvc.perform(put("/airplanes/{id}", id)
                        .content(jsonUpdatedDetails)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedDetails.getId()));
    }

    @Test
    public void whenUpdateNonExistingAirplane_thenNotFound() throws Exception {
        // Arrange
        Long id = 1L;
        Airplane updatedDetails = new Airplane(/* ... */);
        String jsonUpdatedDetails = objectMapper.writeValueAsString(updatedDetails);
        when(airplaneRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/airplanes/{id}", id)
                        .content(jsonUpdatedDetails)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void whenDeleteExistingAirplane_thenDeleteAndReturnNoContent() throws Exception {
        // Arrange
        Long id = 1L;
        when(airplaneRepository.existsById(id)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/airplanes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void whenDeleteNonExistingAirplane_thenNotFound() throws Exception {
        // Arrange
        Long id = 1L;
        when(airplaneRepository.existsById(id)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/airplanes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void whenGetAirplanesByAirportId_thenReturnMatchingAirplanes() throws Exception {
        // Arrange
        Long airportId = 1L;
        List<Airplane> expectedAirplanes = Arrays.asList(new Airplane(/* ... */), new Airplane(/* ... */));
        when(airplaneRepository.findByAirportId(airportId)).thenReturn(expectedAirplanes);

        // Act & Assert
        mockMvc.perform(get("/airplanes/airport/{airportId}", airportId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(expectedAirplanes.size())));
    }

    @Test
    public void whenGetAirplanesByNonExistingAirportId_thenNotFound() throws Exception {
        // Arrange
        Long airportId = 1L;
        when(airplaneRepository.findByAirportId(airportId)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/airplanes/airport/{airportId}", airportId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void whenGetAirplanesByModel_thenReturnMatchingAirplanes() throws Exception {
        // Arrange
        String model = "Boeing";
        List<Airplane> expectedAirplanes = Arrays.asList(new Airplane(/* ... */), new Airplane(/* ... */));
        when(airplaneRepository.findByModelIgnoreCase(model)).thenReturn(expectedAirplanes);

        // Act & Assert
        mockMvc.perform(get("/airplanes/model/{model}", model)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(expectedAirplanes.size())));
    }

    @Test
    public void whenGetAirplanesByNonExistingModel_thenNotFound() throws Exception {
        // Arrange
        String model = "NonExistingModel";
        when(airplaneRepository.findByModelIgnoreCase(model)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/airplanes/model/{model}", model)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}