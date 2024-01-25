package api.airplaneservice;

import api.airplaneservice.controller.AirplaneController;
import api.airplaneservice.model.Airplane;
import api.airplaneservice.repository.AirplaneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AirplaneControllerTest {

    @Mock
    private AirplaneRepository airplaneRepository;

    @InjectMocks
    private AirplaneController airplaneController;

    @BeforeEach
    public void setup() {
    }

    @Test
    public void whenGetAllAirplanes_thenReturnListOfAirplanes() {
        // Arrange
        List<Airplane> expectedAirplanes = new ArrayList<>();
        Airplane airplane1 = new Airplane();
        airplane1.setId(1L);
        airplane1.setModel("Model1");

        Airplane airplane2 = new Airplane();
        airplane2.setId(2L);
        airplane2.setModel("Model2");

        expectedAirplanes.add(airplane1);
        expectedAirplanes.add(airplane2);

        when(airplaneRepository.findAll()).thenReturn(expectedAirplanes);

        // Act
        List<Airplane> actualAirplanes = airplaneController.getAllAirplanes();

        // Assert
        assertEquals(expectedAirplanes.size(), actualAirplanes.size(), "The size of expected and actual airplanes should match");
        assertEquals(expectedAirplanes, actualAirplanes, "The expected airplanes and actual airplanes lists should match");
        verify(airplaneRepository, times(1)).findAll();
    }

    @Test
    public void whenGetAirplaneByIdWithExistingId_thenReturnAirplane() {
        // Arrange
        Long id = 1L;
        Airplane expectedAirplane = new Airplane();
        expectedAirplane.setId(id);
        expectedAirplane.setModel("Model1");

        when(airplaneRepository.findById(id)).thenReturn(Optional.of(expectedAirplane));

        // Act
        ResponseEntity<Airplane> response = airplaneController.getAirplaneById(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedAirplane, response.getBody());
    }

    @Test
    public void whenCreateAirplane_thenSaveAndReturnAirplane() {
        // Arrange
        Airplane newAirplane = new Airplane();
        newAirplane.setModel("Model1");

        Airplane savedAirplane = new Airplane();
        savedAirplane.setId(1L);
        savedAirplane.setModel("Model1");

        when(airplaneRepository.save(newAirplane)).thenReturn(savedAirplane);

        // Act
        ResponseEntity<Airplane> response = airplaneController.createAirplane(newAirplane);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(savedAirplane, response.getBody());
        verify(airplaneRepository, times(1)).save(newAirplane);
    }

    @Test
    public void whenUpdateExistingAirplane_thenUpdateAndReturnAirplane() {
        // Arrange
        Long id = 1L;
        Airplane existingAirplane = new Airplane();
        existingAirplane.setId(id);
        existingAirplane.setModel("OldModel");

        Airplane updatedAirplane = new Airplane();
        updatedAirplane.setId(id);
        updatedAirplane.setModel("UpdatedModel");

        when(airplaneRepository.findById(id)).thenReturn(Optional.of(existingAirplane));
        when(airplaneRepository.save(existingAirplane)).thenReturn(updatedAirplane);

        // Act
        ResponseEntity<Airplane> response = airplaneController.updateAirplane(id, updatedAirplane);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedAirplane.getModel(), response.getBody().getModel());
        verify(airplaneRepository).save(existingAirplane);
    }

    @Test
    public void whenUpdateNonExistingAirplane_thenNotFound() {
        // Arrange
        Long id = 1L;
        Airplane airplaneDetails = new Airplane();
        airplaneDetails.setModel("Model1");

        when(airplaneRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Airplane> response = airplaneController.updateAirplane(id, airplaneDetails);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void whenDeleteExistingAirplane_thenDeleteAndReturnNoContent() {
        // Arrange
        Long id = 1L;
        when(airplaneRepository.existsById(id)).thenReturn(true);
        doNothing().when(airplaneRepository).deleteById(id);

        // Act
        ResponseEntity<HttpStatus> response = airplaneController.deleteAirplane(id);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(airplaneRepository).deleteById(id);
    }

    @Test
    public void whenDeleteNonExistingAirplane_thenNotFound() {
        // Arrange
        Long id = 1L;
        when(airplaneRepository.existsById(id)).thenReturn(false);

        // Act
        ResponseEntity<HttpStatus> response = airplaneController.deleteAirplane(id);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void whenGetAirplanesByAirportId_thenReturnMatchingAirplanes() {
        // Arrange
        Long airportId = 1L;
        List<Airplane> expectedAirplanes = Arrays.asList(new Airplane(), new Airplane());
        when(airplaneRepository.findByAirportId(airportId)).thenReturn(expectedAirplanes);

        // Act
        ResponseEntity<List<Airplane>> response = airplaneController.getAirplanesByAirportId(airportId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedAirplanes.size(), response.getBody().size());
    }

    @Test
    public void whenGetAirplanesByNonExistingAirportId_thenNotFound() {
        // Arrange
        Long airportId = 1L;
        when(airplaneRepository.findByAirportId(airportId)).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<List<Airplane>> response = airplaneController.getAirplanesByAirportId(airportId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void whenGetAirplanesByModel_thenReturnMatchingAirplanes() {
        // Arrange
        String model = "Boeing";
        List<Airplane> expectedAirplanes = Arrays.asList(new Airplane(), new Airplane());
        when(airplaneRepository.findByModelIgnoreCase(model)).thenReturn(expectedAirplanes);

        // Act
        ResponseEntity<List<Airplane>> response = airplaneController.getAirplanesByModel(model);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedAirplanes.size(), response.getBody().size());
    }

    @Test
    public void whenGetAirplanesByNonExistingModel_thenNotFound() {
        // Arrange
        String model = "NonExistingModel";
        when(airplaneRepository.findByModelIgnoreCase(model)).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<List<Airplane>> response = airplaneController.getAirplanesByModel(model);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
