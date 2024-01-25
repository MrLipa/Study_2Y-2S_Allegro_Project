package api.reservationservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "flight")
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "airplane_id")
    private Long airplaneId;

    @Column(name = "start_airport_id")
    private Long startAirportId;

    @Column(name = "destination_airport_id")
    private Long destinationAirportId;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "arrival_date", nullable = false)
    private LocalDateTime arrivalDate;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "number_of_available_seats", nullable = false)
    private int numberOfAvailableSeats;

    @Column(name = "description")
    private String description;
}