package api.flightservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import api.flightservice.model.Flight;


@Repository
public interface FlightRepository extends JpaRepository<Flight, Long>,  JpaSpecificationExecutor<Flight> {
}