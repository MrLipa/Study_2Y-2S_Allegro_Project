package api.reservationservice;

import api.reservationservice.controller.ReservationController;
import api.reservationservice.model.Flight;
import api.reservationservice.model.Reservation;
import api.reservationservice.model.User;
import api.reservationservice.repository.FlightRepository;
import api.reservationservice.repository.ReservationRepository;
import api.reservationservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ReservationControllerTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private ReservationController reservationController;

    private User mockUser;
    private UserDetails mockUserDetails;
    private Authentication authentication;
    private SecurityContext securityContext;

    @BeforeEach
    public void setUp() {
        // Create a mock user
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");
        mockUser.setEmail("test@example.com");

        // Set up the UserDetails and Authentication objects
        mockUserDetails = mock(UserDetails.class);
        lenient().when(mockUserDetails.getUsername()).thenReturn(mockUser.getId().toString());

        authentication = mock(Authentication.class);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getPrincipal()).thenReturn(mockUserDetails);

        securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }


    @Test
    public void whenGetAllReservationsOfCurrentUser_thenShouldReturnReservations() {
        List<Reservation> reservations = new ArrayList<>();
        reservations.add(new Reservation());
        when(reservationRepository.findAllByUserId(mockUser.getId())).thenReturn(reservations);

        ResponseEntity<List<Reservation>> response = reservationController.getAllReservationsOfCurrentUser();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reservations.size(), response.getBody().size());
        verify(reservationRepository).findAllByUserId(mockUser.getId());
    }

    @Test
    public void whenCreateReservation_thenShouldReturnCreatedReservation() {
        Reservation reservation = new Reservation();
        reservation.setUserId(mockUser.getId());
        reservation.setFlight(new Flight());
        reservation.setReservationDate(new Timestamp(System.currentTimeMillis()));

        when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));
        when(flightRepository.findById(anyLong())).thenReturn(Optional.of(new Flight()));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        ResponseEntity<?> response = reservationController.createReservation(1L);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    public void whenDeleteReservation_thenShouldReturnNoContent() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUserId(mockUser.getId());
        reservation.setFlight(new Flight());
        reservation.setReservationDate(new Timestamp(System.currentTimeMillis()));

        when(reservationRepository.findByIdAndUserId(reservation.getId(), mockUser.getId())).thenReturn(Optional.of(reservation));
        doNothing().when(reservationRepository).deleteById(reservation.getId());

        ResponseEntity<?> response = reservationController.deleteReservation(reservation.getId());

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(reservationRepository).deleteById(reservation.getId());
    }

    @Test
    public void whenGetAllReservationsOfCurrentUserWithNoAuth_thenShouldReturnUnauthorized() {
        // Arrange: Set up an unauthenticated context
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        ResponseEntity<List<Reservation>> response = reservationController.getAllReservationsOfCurrentUser();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void whenCreateReservationWithNoAuth_thenShouldReturnUnauthorized() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        ResponseEntity<?> response = reservationController.createReservation(1L);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Unauthorized - User is not authenticated"));
    }

    @Test
    public void whenDeleteReservationWithNoAuth_thenShouldReturnUnauthorized() {
        // Arrange
        Long reservationId = 1L;
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        ResponseEntity<?> response = reservationController.deleteReservation(reservationId);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Unauthorized - User is not authenticated"));
    }

    @Test
    public void whenDeleteNonExistingReservation_thenShouldReturnNotFound() {
        // Arrange
        Long reservationId = 999L;
        when(reservationRepository.findByIdAndUserId(reservationId, mockUser.getId())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = reservationController.deleteReservation(reservationId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Reservation not found or does not belong to the current user"));
    }

}
