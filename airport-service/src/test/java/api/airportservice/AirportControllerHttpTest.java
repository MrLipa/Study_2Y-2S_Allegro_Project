package api.airportservice;

import api.airportservice.controller.AirportController;
import api.airportservice.model.Airport;
import api.airportservice.repository.AirportRepository;
import api.airportservice.repository.UserRepository;
import api.airportservice.security.JwtUtils;
import api.airportservice.security.SecurityConfig;
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
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AirportController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension.class)
@Import(SecurityConfig.class)
public class AirportControllerHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AirportRepository airportRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    public void whenGetAllAirports_thenReturnListOfAirports() throws Exception {
        // Arrange
        List<Airport> airports = new ArrayList<>();
        airports.add(new Airport(/* ... */));
        airports.add(new Airport(/* ... */));
        when(airportRepository.findAll()).thenReturn(airports);

        // Act & Assert
        mockMvc.perform(get("/airports")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(airports.size())));
    }

    @Test
    public void whenGetAirportByIdWithExistingId_thenReturnAirport() throws Exception {
        // Arrange
        Long id = 1L;
        Airport airport = new Airport(/* ... */);
        when(airportRepository.findById(id)).thenReturn(Optional.of(airport));

        // Act & Assert
        mockMvc.perform(get("/airports/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(airport.getId()));
    }

    @Test
    public void whenGetAirportByIdWithNonExistingId_thenNotFound() throws Exception {
        // Arrange
        Long id = 1L;
        when(airportRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/airports/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void whenCreateAirport_thenSaveAndReturnAirport() throws Exception {
        // Arrange
        Airport airport = new Airport(/* ... */);
        String jsonAirport = objectMapper.writeValueAsString(airport);
        when(airportRepository.save(airport)).thenReturn(airport);

        // Act & Assert
        mockMvc.perform(post("/airports")
                        .content(jsonAirport)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(airport.getId()));
    }

    @Test
    public void whenUpdateExistingAirport_thenUpdateAndReturnAirport() throws Exception {
        // Arrange
        Long id = 1L;
        Airport existingAirport = new Airport(/* ... */);
        Airport updatedDetails = new Airport(/* ... */);
        String jsonUpdatedDetails = objectMapper.writeValueAsString(updatedDetails);
        when(airportRepository.findById(id)).thenReturn(Optional.of(existingAirport));
        when(airportRepository.save(any(Airport.class))).thenReturn(updatedDetails);

        // Act & Assert
        mockMvc.perform(put("/airports/{id}", id)
                        .content(jsonUpdatedDetails)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedDetails.getId()));
    }

    @Test
    public void whenUpdateNonExistingAirport_thenNotFound() throws Exception {
        // Arrange
        Long id = 1L;
        Airport updatedDetails = new Airport(/* ... */);
        String jsonUpdatedDetails = objectMapper.writeValueAsString(updatedDetails);
        when(airportRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/airports/{id}", id)
                        .content(jsonUpdatedDetails)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void whenDeleteExistingAirport_thenDeleteAndReturnNoContent() throws Exception {
        // Arrange
        Long id = 1L;
        doNothing().when(airportRepository).deleteById(id);

        // Act & Assert
        mockMvc.perform(delete("/airports/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void whenDeleteAirportAndExceptionOccurs_thenInternalServerError() throws Exception {
        // Arrange
        Long id = 1L;
        doThrow(new RuntimeException("Database error")).when(airportRepository).deleteById(id);

        // Act & Assert
        mockMvc.perform(delete("/airports/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void whenGetAirportsByName_thenReturnMatchingAirports() throws Exception {
        // Arrange
        String name = "TestAirportName";
        Airport airport1 = new Airport();
        airport1.setName(name);
        Airport airport2 = new Airport();
        airport2.setName(name);
        List<Airport> expectedAirports = Arrays.asList(airport1, airport2);
        when(airportRepository.findByNameContainingIgnoreCase(name)).thenReturn(expectedAirports);

        // Act & Assert
        mockMvc.perform(get("/airports/search/name/{name}", name)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(expectedAirports.size())))
                .andExpect(jsonPath("$[0].name", containsString(name))); // Additional checks can be added
    }

    @Test
    public void whenGetAirportsByCountry_thenReturnMatchingAirports() throws Exception {
        // Arrange
        String country = "TestCountry";
        Airport airport1 = new Airport();
        airport1.setCountry(country);
        Airport airport2 = new Airport();
        airport2.setCountry(country);
        List<Airport> expectedAirports = Arrays.asList(airport1, airport2);
        when(airportRepository.findByCountryContainingIgnoreCase(country)).thenReturn(expectedAirports);

        // Act & Assert
        mockMvc.perform(get("/airports/search/country/{country}", country)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(expectedAirports.size())))
                .andExpect(jsonPath("$[0].country", containsString(country))); // Additional checks can be added
    }

    @Test
    public void whenGetAirportsByCity_thenReturnMatchingAirports() throws Exception {
        // Arrange
        String city = "TestCity";
        Airport airport1 = new Airport();
        airport1.setCity(city);
        Airport airport2 = new Airport();
        airport2.setCity(city);
        List<Airport> expectedAirports = Arrays.asList(airport1, airport2);
        when(airportRepository.findByCityContainingIgnoreCase(city)).thenReturn(expectedAirports);

        // Act & Assert
        mockMvc.perform(get("/airports/search/city/{city}", city)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(expectedAirports.size())))
                .andExpect(jsonPath("$[0].city", containsString(city))); // Additional checks can be added
    }
}
