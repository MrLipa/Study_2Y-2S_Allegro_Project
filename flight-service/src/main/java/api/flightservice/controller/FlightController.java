package api.flightservice.controller;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import api.flightservice.model.Airplane;
import api.flightservice.model.Airport;
import api.flightservice.model.Flight;
import api.flightservice.model.dto.FlightDTO;
import api.flightservice.model.specification.FlightSpecification;
import api.flightservice.model.specification.SearchCriteria;
import api.flightservice.repository.AirplaneRepository;
import api.flightservice.repository.AirportRepository;
import api.flightservice.repository.FlightRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/flights")
public class FlightController {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private AirplaneRepository airplaneRepository;

    @Autowired
    private AirportRepository airportRepository;

    @Operation(summary = "Get all flights", description = "Retrieve a list of all flights")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping
    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    @Operation(summary = "Get flight by ID", description = "Retrieve a specific flight by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the flight"),
            @ApiResponse(responseCode = "404", description = "Flight not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Flight> getFlightById(@PathVariable Long id) {
        Optional<Flight> flight = flightRepository.findById(id);
        return flight.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new flight", description = "Creates a new flight with the provided flight details. This operation requires a FlightDTO object with details like airplane ID, start and destination airports, start and arrival dates, price, and an optional description.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Flight created successfully, returns the created Flight object."),
            @ApiResponse(responseCode = "400", description = "Bad Request, possibly due to invalid airplane ID or invalid airport IDs."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error, possible issues with server or database.")
    })
    @PostMapping
    public ResponseEntity<?> createFlight(@Valid @RequestBody FlightDTO flightDTO) {
        Optional<Airplane> airplaneOptional = airplaneRepository.findById(flightDTO.getAirplaneId());
        if (airplaneOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid airplane ID: " + flightDTO.getAirplaneId());
        }
        Airplane airplane = airplaneOptional.get();

        Optional<Airport> startAirportOptional = airportRepository.findById(flightDTO.getStartAirportId());
        if (startAirportOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid start airport ID: " + flightDTO.getStartAirportId());
        }
        Airport startAirport = startAirportOptional.get();

        Optional<Airport> destinationAirportOptional = airportRepository.findById(flightDTO.getDestinationAirportId());
        if (destinationAirportOptional.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Invalid destination airport ID: " + flightDTO.getDestinationAirportId());
        }
        Airport destinationAirport = destinationAirportOptional.get();

        Flight flight = new Flight();
        flight.setAirplane(airplane);
        flight.setNumberOfAvailableSeats(airplane.getNumberOfSeats());
        flight.setStartAirport(startAirport);
        flight.setDestinationAirport(destinationAirport);
        flight.setStartDate(flightDTO.getStartDate());
        flight.setArrivalDate(flightDTO.getArrivalDate());
        flight.setPrice(flightDTO.getPrice());
        flight.setDescription(flightDTO.getDescription());

        Flight savedFlight = flightRepository.save(flight);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedFlight.getId())
                .toUri();

        return ResponseEntity.created(location).body(savedFlight);
    }

    @Operation(summary = "Update an existing flight", description = "Updates an existing flight identified by its ID with the provided flight details. Only non-null fields in the FlightDTO will be updated. Fields left null will retain their current values in the database, effectively allowing partial updates of the flight entity.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flight updated successfully, returns the updated Flight object."),
            @ApiResponse(responseCode = "400", description = "Bad Request, possibly due to invalid input like non-existent airplane ID or airport IDs."),
            @ApiResponse(responseCode = "404", description = "Flight not found with the provided ID."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error, possible issues with server or database.")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFlight(@PathVariable Long id, @Valid @RequestBody FlightDTO flightDTO) {
        return flightRepository.findById(id)
                .map(flight -> {
                    if (flightDTO.getAirplaneId() != null) {
                        Optional<Airplane> airplane = airplaneRepository.findById(flightDTO.getAirplaneId());
                        airplane.ifPresent(flight::setAirplane);
                    }

                    if (flightDTO.getStartAirportId() != null) {
                        Optional<Airport> startAirport = airportRepository.findById(flightDTO.getStartAirportId());
                        startAirport.ifPresent(flight::setStartAirport);
                    }

                    if (flightDTO.getDestinationAirportId() != null) {
                        Optional<Airport> destinationAirport = airportRepository
                                .findById(flightDTO.getDestinationAirportId());
                        destinationAirport.ifPresent(flight::setDestinationAirport);
                    }

                    if (flightDTO.getStartDate() != null) {
                        flight.setStartDate(flightDTO.getStartDate());
                    }

                    if (flightDTO.getArrivalDate() != null) {
                        flight.setArrivalDate(flightDTO.getArrivalDate());
                    }

                    if (flightDTO.getPrice() != null) {
                        flight.setPrice(flightDTO.getPrice());
                    }

                    if (flightDTO.getDescription() != null) {
                        flight.setDescription(flightDTO.getDescription());
                    }

                    Flight updatedFlight = flightRepository.save(flight);
                    return ResponseEntity.ok(updatedFlight);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete a flight", description = "Delete a flight with the provided ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Flight deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Flight not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteFlight(@PathVariable Long id) {
        if (!flightRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        flightRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Search flights", description = "Search for flights based on various criteria such as airplane, start and destination airports, dates, and price range")
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
    @PostMapping("/search")
    public ResponseEntity<List<Flight>> searchFlights(@RequestBody List<SearchCriteria> searchCriteriaList) {
        Specification<Flight> spec = Specification.where(null);

        for (SearchCriteria criteria : searchCriteriaList) {
            spec = spec.and(new FlightSpecification(criteria));
        }

        List<Flight> results = flightRepository.findAll(spec);
        return ResponseEntity.ok(results);
    }
}
