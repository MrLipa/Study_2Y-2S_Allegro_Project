package api.reservationservice;

import api.reservationservice.controller.ReservationController;
import api.reservationservice.model.Flight;
import api.reservationservice.model.Reservation;
import api.reservationservice.model.User;
import api.reservationservice.repository.FlightRepository;
import api.reservationservice.repository.ReservationRepository;
import api.reservationservice.repository.UserRepository;
import api.reservationservice.security.JwtUtils;
import api.reservationservice.security.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;


import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension.class)
@Import(SecurityConfig.class)
public class ReservationControllerHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationRepository reservationRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private FlightRepository flightRepository;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    @WithMockUser(username = "1")
    public void getAllReservationsOfCurrentUserTest() throws Exception {
        Reservation reservation1 = new Reservation();
        Reservation reservation2 = new Reservation();
        when(reservationRepository.findAllByUserId(anyLong())).thenReturn(Arrays.asList(reservation1, reservation2));

        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "1")
    public void createReservationTest() throws Exception {
        Reservation reservation = new Reservation();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(new User()));
        when(flightRepository.findById(anyLong())).thenReturn(Optional.of(new Flight()));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        mockMvc.perform(post("/reservations/{flightId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservation)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(reservation)));
    }

    @Test
    @WithMockUser(username = "1")
    public void deleteReservationTest() throws Exception {
        when(reservationRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(new Reservation()));
        doNothing().when(reservationRepository).deleteById(anyLong());

        mockMvc.perform(delete("/reservations/{reservationId}", 1L))
                .andExpect(status().isNoContent());
    }
}
