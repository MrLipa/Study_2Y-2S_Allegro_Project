package api.flightservice.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FlightDTO {

    private Long airplaneId;
    private Long startAirportId;
    private Long destinationAirportId;
    private LocalDateTime startDate;
    private LocalDateTime arrivalDate;
    private BigDecimal price;
    private String description;
}
