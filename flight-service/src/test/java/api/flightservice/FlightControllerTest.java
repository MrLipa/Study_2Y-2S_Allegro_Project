package api.flightservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import api.flightservice.controller.FlightController;
import api.flightservice.model.Airplane;
import api.flightservice.model.Airport;
import api.flightservice.model.Flight;
import api.flightservice.model.dto.FlightDTO;
import api.flightservice.repository.AirplaneRepository;
import api.flightservice.repository.AirportRepository;
import api.flightservice.repository.FlightRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    // @Test
    // public void testCreateFlight_Success() {
    //     // Arrange
    //     FlightDTO flightDTO = new FlightDTO();
    //     Airplane airplane = new Airplane();
    //     Airport startAirport = new Airport();
    //     Airport destinationAirport = new Airport();

    //     when(airplaneRepository.findById(anyLong())).thenReturn(Optional.of(airplane));
    //     when(airportRepository.findById(flightDTO.getStartAirportId())).thenReturn(Optional.of(startAirport));
    //     when(airportRepository.findById(flightDTO.getDestinationAirportId()))
    //             .thenReturn(Optional.of(destinationAirport));
    //     when(flightRepository.save(any(Flight.class))).thenReturn(new Flight());

    //     MockHttpServletRequest request = new MockHttpServletRequest();
    //     RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    //     // Act
    //     ResponseEntity<?> response = flightController.createFlight(flightDTO);

    //     // Assert
    //     assertNotNull(response.getBody());
    //     assertEquals(HttpStatus.CREATED, response.getStatusCode());

    //     verify(airplaneRepository, times(1)).findById(anyLong());
    //     verify(airportRepository, times(2)).findById(anyLong());
    //     verify(flightRepository, times(1)).save(any(Flight.class));
    // }
}
