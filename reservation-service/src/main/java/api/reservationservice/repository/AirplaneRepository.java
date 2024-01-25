package api.reservationservice.repository;

import api.reservationservice.model.Airplane;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AirplaneRepository extends JpaRepository<Airplane, Long> {
    List<Airplane> findByAirportId(Long airportId);
    List<Airplane> findByModelIgnoreCase(String model);
}
