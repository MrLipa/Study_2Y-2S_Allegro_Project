package api.flightservice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import api.flightservice.model.Flight;
import api.flightservice.model.dto.FlightDTO;
import api.flightservice.model.specification.SearchCriteria;

import java.net.URI;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled("h2-not-working")
public class FlightControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getUrl() {
        return "http://localhost:" + port + "/flights";
    }

    @Test
    public void testGetAllFlights() {
        ResponseEntity<Flight[]> response = restTemplate.getForEntity(getUrl(), Flight[].class);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().length > 0);
    }

    @Test
    public void testGetFlightById() {
        ResponseEntity<Flight> response = restTemplate.getForEntity(getUrl() + "/1", Flight.class);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testCreateFlight() {
        FlightDTO flightDTO = new FlightDTO(); 
        ResponseEntity<Flight> response = restTemplate.postForEntity(getUrl(), flightDTO, Flight.class);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void testUpdateFlight() {
        FlightDTO flightDTO = new FlightDTO(); 
        ResponseEntity<Flight> response = restTemplate.postForEntity(getUrl(), flightDTO, Flight.class);
        assertNotNull(response.getBody());
        
        flightDTO.setDescription("Updated Description");
        restTemplate.put(URI.create(getUrl() + "/" + response.getBody().getId()), flightDTO);
        
        ResponseEntity<Flight> updatedResponse = restTemplate.getForEntity(getUrl() + "/" + response.getBody().getId(), Flight.class);
        assertNotNull(updatedResponse.getBody());
        assertEquals("Updated Description", updatedResponse.getBody().getDescription());
    }

    @Test
    public void testDeleteFlight() {
        restTemplate.delete(URI.create(getUrl() + "/1"));
        ResponseEntity<Flight> response = restTemplate.getForEntity(getUrl() + "/1", Flight.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testSearchFlights() {
        SearchCriteria[] criteria = {
            new SearchCriteria("price", "<=", new BigDecimal("500.00")),
        };
        ResponseEntity<Flight[]> response = restTemplate.postForEntity(getUrl() + "/search", criteria, Flight[].class);
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
