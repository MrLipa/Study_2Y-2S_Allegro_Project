package api.airplaneservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import api.airplaneservice.model.Airplane;

@Repository
public interface AirplaneRepository extends JpaRepository<Airplane, Long> {
    List<Airplane> findByAirportId(Long airportId);
    List<Airplane> findByModelIgnoreCase(String model);
}
