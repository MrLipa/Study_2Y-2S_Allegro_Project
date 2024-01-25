package api.reservationservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;

@Data
@Entity
@Table(name = "airplane")
public class Airplane {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Temporal(TemporalType.DATE)
    @Column(name = "production_date", nullable = false)
    private Date productionDate;

    @Column(name = "number_of_seats", nullable = false)
    private int numberOfSeats;

    @Column(name = "max_distance", nullable = false)
    private int maxDistance;

    @ManyToOne
    @JoinColumn(name = "airport_id", nullable = false)
    private Airport airport;
}