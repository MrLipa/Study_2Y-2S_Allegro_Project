package api.reservationservice.repository;

import api.reservationservice.model.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AirportRepository extends JpaRepository<Airport, Long> {
    List<Airport> findByNameContainingIgnoreCase(String name);
    List<Airport> findByCountryContainingIgnoreCase(String country);
    List<Airport> findByCityContainingIgnoreCase(String city);
}