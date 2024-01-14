package api.airportservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import api.airportservice.model.Airport;
import api.airportservice.repository.AirportRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/airports")
public class AirportController {

    @Autowired
    private AirportRepository airportRepository;

    @Operation(summary = "Get all airports", description = "Retrieve a list of all airports")
    @GetMapping
    public List<Airport> getAllAirports() {
        return airportRepository.findAll();
    }

    @Operation(summary = "Get airport by ID", description = "Retrieve a specific airport by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the airport"),
            @ApiResponse(responseCode = "404", description = "Airport not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Airport> getAirportById(@PathVariable Long id) {
        Optional<Airport> airport = airportRepository.findById(id);
        return airport.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new airport", description = "Create a new airport with the provided data")
    @ApiResponse(responseCode = "201", description = "Airport created successfully")
    @PostMapping
    public ResponseEntity<Airport> createAirport(@RequestBody Airport airport) {
        Airport savedAirport = airportRepository.save(airport);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAirport);
    }

    @Operation(summary = "Update an existing airport", description = "Update an existing airport with the provided ID and data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Airport updated successfully"),
            @ApiResponse(responseCode = "404", description = "Airport not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Airport> updateAirport(@PathVariable Long id, @RequestBody Airport airportDetails) {
        Optional<Airport> airportData = airportRepository.findById(id);

        if (airportData.isPresent()) {
            Airport updatedAirport = airportData.get();
            updatedAirport.setName(airportDetails.getName());
            updatedAirport.setCountry(airportDetails.getCountry());
            updatedAirport.setCity(airportDetails.getCity());
            updatedAirport.setLongitude(airportDetails.getLongitude());
            updatedAirport.setLatitude(airportDetails.getLatitude());
            updatedAirport.setDescription(airportDetails.getDescription());
            return ResponseEntity.ok(airportRepository.save(updatedAirport));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete an airport", description = "Delete an airport with the provided ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Airport deleted successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteAirport(@PathVariable Long id) {
        try {
            airportRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Search airports by name", description = "Search for airports by their name")
    @GetMapping("/search/name/{name}")
    public List<Airport> getAirportsByName(@PathVariable String name) {
        return airportRepository.findByNameContainingIgnoreCase(name);
    }

    @Operation(summary = "Search airports by country", description = "Search for airports by their country")
    @GetMapping("/search/country/{country}")
    public List<Airport> getAirportsByCountry(@PathVariable String country) {
        return airportRepository.findByCountryContainingIgnoreCase(country);
    }

    @Operation(summary = "Search airports by city", description = "Search for airports by their city")
    @GetMapping("/search/city/{city}")
    public List<Airport> getAirportsByCity(@PathVariable String city) {
        return airportRepository.findByCityContainingIgnoreCase(city);
    }
}
