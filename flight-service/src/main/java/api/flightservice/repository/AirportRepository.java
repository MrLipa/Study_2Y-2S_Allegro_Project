package api.flightservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import api.flightservice.model.Airport;

@Repository
public interface AirportRepository extends JpaRepository<Airport, Long> {
    List<Airport> findByNameContainingIgnoreCase(String name);
    List<Airport> findByCountryContainingIgnoreCase(String country);
    List<Airport> findByCityContainingIgnoreCase(String city);
}