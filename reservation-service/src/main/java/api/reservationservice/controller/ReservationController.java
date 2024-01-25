package api.reservationservice.controller;

import api.reservationservice.model.Flight;
import api.reservationservice.model.Reservation;
import api.reservationservice.model.User;
import api.reservationservice.repository.FlightRepository;
import api.reservationservice.repository.ReservationRepository;
import api.reservationservice.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Operation(summary = "Get all reservations of the current user", description = "Retrieve a list of all reservations made by the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of reservations", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Reservation[].class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated", content = @Content(mediaType = "text/plain"))
    })
    @GetMapping()
    public ResponseEntity<List<Reservation>> getAllReservationsOfCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Long userId = Long.parseLong(((UserDetails) authentication.getPrincipal()).getUsername());
        List<Reservation> reservations = reservationRepository.findAllByUserId(userId);

        return ResponseEntity.ok(reservations);
    }

    @Operation(summary = "Create a new reservation", description = "Creates a new reservation for the currently authenticated user for the specified flight.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reservation created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Reservation.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "404", description = "Flight not found", content = @Content(mediaType = "text/plain"))
    })
    @PostMapping("/{flightId}")
    public ResponseEntity<?> createReservation(@PathVariable Long flightId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>("Unauthorized - User is not authenticated", HttpStatus.UNAUTHORIZED);
        }

        Long userId = Long.parseLong(((UserDetails) authentication.getPrincipal()).getUsername());
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Flight> flightOptional = flightRepository.findById(flightId);

        if (!userOptional.isPresent()) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        if (!flightOptional.isPresent()) {
            return new ResponseEntity<>("Flight not found", HttpStatus.NOT_FOUND);
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setFlight(flightOptional.get());
        reservation.setReservationDate(new Timestamp(System.currentTimeMillis()));
        Reservation savedReservation = reservationRepository.save(reservation);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedReservation);
    }

    @Operation(summary = "Delete a reservation", description = "Deletes a reservation with the provided ID if it belongs to the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reservation deleted successfully", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not own the reservation", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "404", description = "Reservation not found", content = @Content(mediaType = "text/plain"))
    })
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<?> deleteReservation(@PathVariable Long reservationId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new ResponseEntity<>("Unauthorized - User is not authenticated", HttpStatus.UNAUTHORIZED);
        }

        Long userId = Long.parseLong(((UserDetails) authentication.getPrincipal()).getUsername());
        Optional<Reservation> reservationOptional = reservationRepository.findByIdAndUserId(reservationId, userId);

        if (!reservationOptional.isPresent()) {
            return new ResponseEntity<>("Reservation not found or does not belong to the current user", HttpStatus.NOT_FOUND);
        }

        reservationRepository.deleteById(reservationId);
        return new ResponseEntity<>("Reservation deleted successfully", HttpStatus.NO_CONTENT);
    }
}
