package api.flightservice.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "flight")
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "airplane_id", nullable = false)
    private Airplane airplane;

    @ManyToOne
    @JoinColumn(name = "start_airport_id", nullable = false)
    private Airport startAirport;

    @ManyToOne
    @JoinColumn(name = "destination_airport_id", nullable = false)
    private Airport destinationAirport;

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