package api.flightservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import api.flightservice.controller.FlightController;
import api.flightservice.model.Airplane;
import api.flightservice.model.Airport;
import api.flightservice.model.Flight;
import api.flightservice.model.dto.FlightDTO;
import api.flightservice.model.specification.FlightSpecification;
import api.flightservice.model.specification.SearchCriteria;
import api.flightservice.repository.AirplaneRepository;
import api.flightservice.repository.AirportRepository;
import api.flightservice.repository.FlightRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class FlightControllerTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private AirplaneRepository airplaneRepository;

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private FlightController flightController;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllFlights() {
        // Arrange
        Flight flight1 = new Flight();
        Flight flight2 = new Flight();
        when(flightRepository.findAll()).thenReturn(Arrays.asList(flight1, flight2));

        // Act
        List<Flight> response = flightController.getAllFlights();

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());
        verify(flightRepository, times(1)).findAll();
    }

    @Test
    public void testGetFlightById_Found() {
        // Arrange
        Long id = 1L;
        Flight flight = new Flight();
        when(flightRepository.findById(id)).thenReturn(Optional.of(flight));

        // Act
        ResponseEntity<Flight> response = flightController.getFlightById(id);

        // Assert
        assertTrue(response.getBody() != null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(flightRepository, times(1)).findById(id);
    }

    @Test
    public void testGetFlightById_NotFound() {
        // Arrange
        Long id = 1L;
        when(flightRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Flight> response = flightController.getFlightById(id);

        // Assert
        assertNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(flightRepository, times(1)).findById(id);
    }

    @Test
    public void testCreateFlight_Success() {
        // Arrange
        FlightDTO flightDTO = new FlightDTO();
        flightDTO.setAirplaneId(1L);
        flightDTO.setStartAirportId(1L);
        flightDTO.setDestinationAirportId(2L);

        Airplane airplane = new Airplane();
        Airport startAirport = new Airport();
        Airport destinationAirport = new Airport();

        when(airplaneRepository.findById(anyLong())).thenReturn(Optional.of(airplane));
        when(airportRepository.findById(flightDTO.getStartAirportId())).thenReturn(Optional.of(startAirport));
        when(airportRepository.findById(flightDTO.getDestinationAirportId()))
                .thenReturn(Optional.of(destinationAirport));
        when(flightRepository.save(any(Flight.class))).thenReturn(new Flight());

        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        ResponseEntity<?> response = flightController.createFlight(flightDTO);

        // Assert
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        verify(airplaneRepository, times(1)).findById(anyLong());
        verify(airportRepository, times(2)).findById(anyLong());
        verify(flightRepository, times(1)).save(any(Flight.class));
        verify(airplaneRepository, times(1)).findById(anyLong());
        verify(airportRepository, times(1)).findById(flightDTO.getStartAirportId());
        verify(airportRepository, times(1)).findById(flightDTO.getDestinationAirportId());
        verify(flightRepository, times(1)).save(any(Flight.class));

    }

    @Test
    public void testUpdateFlight_Success() {
        // Arrange
        Long id = 1L;
        Flight existingFlight = new Flight();
        FlightDTO flightDTO = new FlightDTO();

        when(flightRepository.findById(id)).thenReturn(Optional.of(existingFlight));
        when(flightRepository.save(any(Flight.class))).thenReturn(existingFlight);

        // Act
        ResponseEntity<?> response = flightController.updateFlight(id, flightDTO);

        // Assert
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(flightRepository, times(1)).findById(id);
        verify(flightRepository, times(1)).save(any(Flight.class));
    }

    @Test
    public void testCreateFlight_AirplaneDoesNotExist() {
        // Arrange
        FlightDTO flightDTO = new FlightDTO();
        flightDTO.setAirplaneId(1L);
        when(airplaneRepository.findById(flightDTO.getAirplaneId())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = flightController.createFlight(flightDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(airplaneRepository, times(1)).findById(flightDTO.getAirplaneId());
        verify(airportRepository, never()).findById(anyLong());
        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    public void testCreateFlight_StartAirportDoesNotExist() {
        // Arrange
        FlightDTO flightDTO = new FlightDTO();
        Airplane airplane = new Airplane();
        flightDTO.setAirplaneId(1L);
        flightDTO.setStartAirportId(1L);
        when(airplaneRepository.findById(flightDTO.getAirplaneId())).thenReturn(Optional.of(airplane));
        when(airportRepository.findById(flightDTO.getStartAirportId())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = flightController.createFlight(flightDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(airplaneRepository, times(1)).findById(flightDTO.getAirplaneId());
        verify(airportRepository, times(1)).findById(flightDTO.getStartAirportId());
        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    public void testUpdateFlight_FlightDoesNotExist() {
        // Arrange
        Long id = 1L;
        FlightDTO flightDTO = new FlightDTO();
        when(flightRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = flightController.updateFlight(id, flightDTO);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(flightRepository, times(1)).findById(id);
        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    public void testDeleteFlight_Success() {
        // Arrange
        Long id = 1L;
        when(flightRepository.existsById(id)).thenReturn(true);

        // Act
        ResponseEntity<HttpStatus> response = flightController.deleteFlight(id);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(flightRepository, times(1)).deleteById(id);
    }

    @Test
    public void testDeleteFlight_NotFound() {
        // Arrange
        Long id = 1L;
        when(flightRepository.existsById(id)).thenReturn(false);

        // Act
        ResponseEntity<HttpStatus> response = flightController.deleteFlight(id);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(flightRepository, never()).deleteById(id);
    }

    @Test
    public void testSearchFlights_Success() {
        // Arrange
        SearchCriteria criteriaDepartureAirport = new SearchCriteria("startAirport", ":", "JFK");
        SearchCriteria criteriaArrivalAirport = new SearchCriteria("destinationAirport", ":", "LAX");
        SearchCriteria criteriaMaxPrice = new SearchCriteria("price", "<=", new BigDecimal("500.00"));

        List<SearchCriteria> searchCriteriaList = Arrays.asList(criteriaDepartureAirport, criteriaArrivalAirport,
                criteriaMaxPrice);

        List<Flight> foundFlights = Arrays.asList(new Flight(), new Flight());
        when(flightRepository.findAll(any(Specification.class))).thenReturn(foundFlights);

        // Act
        ResponseEntity<List<Flight>> response = flightController.searchFlights(searchCriteriaList);

        // Assert
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(flightRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    public void testSearchFlights_NoResults() {
        // Arrange
        List<SearchCriteria> searchCriteriaList = Arrays.asList(
                new SearchCriteria("departure", ":", "NYC"),
                new SearchCriteria("arrival", ":", "LAX"));
        when(flightRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<Flight>> response = flightController.searchFlights(searchCriteriaList);

        // Assert
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(flightRepository, times(1)).findAll(any(Specification.class));
    }

}
