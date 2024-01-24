package api.airportservice;

import api.airportservice.controller.AirportController;
import api.airportservice.model.Airport;
import api.airportservice.model.Role;
import api.airportservice.model.User;
import api.airportservice.repository.AirportRepository;
import api.airportservice.repository.UserRepository;
import api.airportservice.security.JwtAuthenticationFilter;
import api.airportservice.security.JwtUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AirportControllerTest {

    @Mock
    private AirportRepository airportRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private Validator validator;

    @InjectMocks
    private AirportController airportController;

    @BeforeEach
    public void setup() {
    }

    @Test
    public void whenGetAllAirports_thenReturnListOfAirports() {
        // Arrange
        List<Airport> expectedAirports = new ArrayList<>();
        expectedAirports.add(new Airport());
        expectedAirports.add(new Airport());
        when(airportRepository.findAll()).thenReturn(expectedAirports);

        // Act
        List<Airport> actualAirports = airportController.getAllAirports();

        // Assert
        assertEquals(expectedAirports.size(), actualAirports.size(), "The size of expected and actual airports should match");
        assertEquals(expectedAirports, actualAirports, "The expected airports and actual airports lists should match");
        verify(airportRepository, times(1)).findAll();
    }

    @Test
    public void whenGetAirportByIdWithExistingId_thenReturnAirport() {
        // Arrange
        Long id = 1L;
        Airport expectedAirport = new Airport();
        when(airportRepository.findById(id)).thenReturn(Optional.of(expectedAirport));

        // Act
        ResponseEntity<Airport> response = airportController.getAirportById(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedAirport, response.getBody());
    }

    @Test
    public void whenGetAirportByIdWithNonExistingId_thenNotFound() {
        // Arrange
        Long id = 1L;
        when(airportRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Airport> response = airportController.getAirportById(id);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void whenCreateAirport_thenSaveAndReturnAirport() {
        // Arrange
        Airport newAirport = new Airport();
        Airport savedAirport = new Airport();
        when(airportRepository.save(newAirport)).thenReturn(savedAirport);

        // Act
        ResponseEntity<Airport> response = airportController.createAirport(newAirport);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(savedAirport, response.getBody());
        verify(airportRepository, times(1)).save(newAirport);
    }

    @Test
    public void whenUpdateExistingAirport_thenUpdateAndReturnAirport() {
        // Arrange
        Long id = 1L;
        Airport existingAirport = new Airport();
        Airport updatedDetails = new Airport();
        when(airportRepository.findById(id)).thenReturn(Optional.of(existingAirport));
        when(airportRepository.save(any(Airport.class))).thenReturn(updatedDetails);

        // Act
        ResponseEntity<Airport> response = airportController.updateAirport(id, updatedDetails);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedDetails, response.getBody());
        verify(airportRepository, times(1)).save(existingAirport);
    }

    @Test
    public void whenUpdateNonExistingAirport_thenNotFound() {
        // Arrange
        Long id = 1L;
        Airport updatedDetails = new Airport();
        when(airportRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Airport> response = airportController.updateAirport(id, updatedDetails);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void whenDeleteExistingAirport_thenDeleteAndReturnNoContent() {
        // Arrange
        Long id = 1L;
        doNothing().when(airportRepository).deleteById(id);

        // Act
        ResponseEntity<HttpStatus> response = airportController.deleteAirport(id);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(airportRepository, times(1)).deleteById(id);
    }

    @Test
    public void whenDeleteAirportAndExceptionOccurs_thenInternalServerError() {
        // Arrange
        Long id = 1L;
        doThrow(new RuntimeException("Database error")).when(airportRepository).deleteById(id);

        // Act
        ResponseEntity<HttpStatus> response = airportController.deleteAirport(id);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void whenGetAirportsByName_thenReturnMatchingAirports() {
        // Arrange
        String name = "TestName";
        List<Airport> expectedAirports = Arrays.asList(
                new Airport(),
                new Airport()
        );
        when(airportRepository.findByNameContainingIgnoreCase(name)).thenReturn(expectedAirports);

        // Act
        List<Airport> actualAirports = airportController.getAirportsByName(name);

        // Assert
        assertEquals(expectedAirports.size(), actualAirports.size(), "The size of expected and actual airports should match");
        assertEquals(expectedAirports, actualAirports, "The expected airports and actual airports lists should match");
        verify(airportRepository, times(1)).findByNameContainingIgnoreCase(name);
    }

    @Test
    public void whenGetAirportsByCountry_thenReturnMatchingAirports() {
        // Arrange
        String country = "TestCountry";
        List<Airport> expectedAirports = Arrays.asList(
                new Airport(),
                new Airport()
        );
        when(airportRepository.findByCountryContainingIgnoreCase(country)).thenReturn(expectedAirports);

        // Act
        List<Airport> actualAirports = airportController.getAirportsByCountry(country);

        // Assert
        assertEquals(expectedAirports.size(), actualAirports.size(), "The size of expected and actual airports should match");
        assertEquals(expectedAirports, actualAirports, "The expected airports and actual airports lists should match");
        verify(airportRepository, times(1)).findByCountryContainingIgnoreCase(country);
    }

    @Test
    public void whenGetAirportsByCity_thenReturnMatchingAirports() {
        // Arrange
        String city = "TestCity";
        List<Airport> expectedAirports = Arrays.asList(
                new Airport(),
                new Airport()
        );
        when(airportRepository.findByCityContainingIgnoreCase(city)).thenReturn(expectedAirports);

        // Act
        List<Airport> actualAirports = airportController.getAirportsByCity(city);

        // Assert
        assertEquals(expectedAirports.size(), actualAirports.size(), "The size of expected and actual airports should match");
        assertEquals(expectedAirports, actualAirports, "The expected airports and actual airports lists should match");
        verify(airportRepository, times(1)).findByCityContainingIgnoreCase(city);
    }

}
