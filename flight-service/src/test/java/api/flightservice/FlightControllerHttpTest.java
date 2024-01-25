package api.flightservice;

import api.flightservice.controller.FlightController;
import api.flightservice.model.Airplane;
import api.flightservice.model.Airport;
import api.flightservice.model.Flight;
import api.flightservice.model.dto.FlightDTO;
import api.flightservice.model.specification.SearchCriteria;
import api.flightservice.repository.AirplaneRepository;
import api.flightservice.repository.AirportRepository;
import api.flightservice.repository.FlightRepository;
import api.flightservice.repository.UserRepository;
import api.flightservice.security.JwtUtils;
import api.flightservice.security.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlightController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension.class)
@Import(SecurityConfig.class)
public class FlightControllerHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private FlightController flightController;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FlightRepository flightRepository;

    @MockBean
    private AirplaneRepository airplaneRepository;

    @MockBean
    private AirportRepository airportRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtils jwtUtils;


    @Test
    public void whenGetAllFlights_thenReturnListOfFlights() throws Exception {
        Flight flight1 = new Flight();
        Flight flight2 = new Flight();
        when(flightRepository.findAll()).thenReturn(Arrays.asList(flight1, flight2));

        mockMvc.perform(get("/flights")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void whenGetFlightByIdWithExistingId_thenReturnFlight() throws Exception {
        Long id = 1L;
        Flight flight = new Flight();
        when(flightRepository.findById(id)).thenReturn(Optional.of(flight));

        mockMvc.perform(get("/flights/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void whenGetFlightByIdWithNonExistingId_thenNotFound() throws Exception {
        Long id = 1L;
        when(flightRepository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/flights/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void whenCreateFlightWithValidData_thenCreateFlight() throws Exception {
        FlightDTO flightDTO = new FlightDTO();
        flightDTO.setAirplaneId(1L);
        flightDTO.setStartAirportId(1L);
        flightDTO.setDestinationAirportId(2L);

        Airplane airplane = new Airplane();
        Airport startAirport = new Airport();
        Airport destinationAirport = new Airport();
        when(airplaneRepository.findById(anyLong())).thenReturn(Optional.of(airplane));
        when(airportRepository.findById(anyLong())).thenReturn(Optional.of(startAirport)).thenReturn(Optional.of(destinationAirport));
        when(flightRepository.save(any(Flight.class))).thenReturn(new Flight());

        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flightDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    public void whenUpdateExistingFlight_thenUpdateFlight() throws Exception {
        Long id = 1L;
        Flight existingFlight = new Flight();
        FlightDTO flightDTO = new FlightDTO();

        when(flightRepository.findById(id)).thenReturn(Optional.of(existingFlight));
        when(airplaneRepository.findById(anyLong())).thenReturn(Optional.of(new Airplane()));
        when(airportRepository.findById(anyLong())).thenReturn(Optional.of(new Airport()));
        when(flightRepository.save(any(Flight.class))).thenReturn(existingFlight);

        mockMvc.perform(put("/flights/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flightDTO)))
                .andExpect(status().isOk());
    }

    @Test
    public void whenUpdateNonExistingFlight_thenNotFound() throws Exception {
        Long id = 1L;
        FlightDTO flightDTO = new FlightDTO();

        when(flightRepository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(put("/flights/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flightDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void whenDeleteExistingFlight_thenDeleteFlight() throws Exception {
        Long id = 1L;
        when(flightRepository.existsById(id)).thenReturn(true);

        mockMvc.perform(delete("/flights/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void whenDeleteNonExistingFlight_thenNotFound() throws Exception {
        Long id = 1L;
        when(flightRepository.existsById(id)).thenReturn(false);

        mockMvc.perform(delete("/flights/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void whenSearchFlightsWithCriteria_thenReturnMatchingFlights() throws Exception {
        List<SearchCriteria> searchCriteriaList = Arrays.asList(/* criteria instances */);
        List<Flight> foundFlights = Arrays.asList(new Flight(), new Flight());
        when(flightRepository.findAll(any(Specification.class))).thenReturn(foundFlights);

        mockMvc.perform(post("/flights/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchCriteriaList)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(foundFlights.size())));
    }

    @Test
    public void whenSearchFlightsWithNoMatchingCriteria_thenReturnEmptyList() throws Exception {
        List<SearchCriteria> searchCriteriaList = Arrays.asList(/* criteria instances */);
        when(flightRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/flights/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchCriteriaList)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

}
