package api.airplaneservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import api.airplaneservice.model.Airplane;
import api.airplaneservice.repository.AirplaneRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/airplanes")
public class AirplaneController {

    @Autowired
    private AirplaneRepository airplaneRepository;

    @Operation(summary = "Get all airplanes", description = "Retrieve a list of all airplanes")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping
    public List<Airplane> getAllAirplanes() {
        return airplaneRepository.findAll();
    }

    @Operation(summary = "Get airplane by ID", description = "Retrieve a specific airplane by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the airplane"),
            @ApiResponse(responseCode = "404", description = "Airplane not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Airplane> getAirplaneById(@PathVariable Long id) {
        Optional<Airplane> airplane = airplaneRepository.findById(id);
        return airplane.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new airplane", description = "Create a new airplane with the provided data")
    @ApiResponse(responseCode = "201", description = "Airplane created successfully")
    @PostMapping
    public ResponseEntity<Airplane> createAirplane(@RequestBody Airplane airplane) {
        Airplane savedAirplane = airplaneRepository.save(airplane);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAirplane);
    }

    @Operation(summary = "Update an existing airplane", description = "Update an existing airplane with the provided ID and data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Airplane updated successfully"),
            @ApiResponse(responseCode = "404", description = "Airplane not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Airplane> updateAirplane(@PathVariable Long id, @RequestBody Airplane airplaneDetails) {
        return airplaneRepository.findById(id)
                .map(airplane -> {
                    airplane.setModel(airplaneDetails.getModel());
                    airplane.setProductionDate(airplaneDetails.getProductionDate());
                    airplane.setNumberOfSeats(airplaneDetails.getNumberOfSeats());
                    airplane.setMaxDistance(airplaneDetails.getMaxDistance());
                    airplane.setAirport(airplaneDetails.getAirport());
                    return ResponseEntity.ok(airplaneRepository.save(airplane));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete an airplane", description = "Delete an airplane with the provided ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Airplane deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Airplane not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteAirplane(@PathVariable Long id) {
        if (!airplaneRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        airplaneRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Find all airplanes at a specific airport", description = "Retrieve all airplanes currently located at an airport with the given ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved airplanes"),
            @ApiResponse(responseCode = "404", description = "Airport not found")
    })
    @GetMapping("/airport/{airportId}")
    public ResponseEntity<List<Airplane>> getAirplanesByAirportId(@PathVariable Long airportId) {
        List<Airplane> airplanes = airplaneRepository.findByAirportId(airportId);
        if (airplanes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(airplanes);
    }

    @Operation(summary = "Find airplanes by model", description = "Retrieve all airplanes of a specific model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved airplanes"),
            @ApiResponse(responseCode = "404", description = "No airplanes found for the specified model")
    })
    @GetMapping("/model/{model}")
    public ResponseEntity<List<Airplane>> getAirplanesByModel(@PathVariable String model) {
        List<Airplane> airplanes = airplaneRepository.findByModelIgnoreCase(model);
        if (airplanes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(airplanes);
    }
}
