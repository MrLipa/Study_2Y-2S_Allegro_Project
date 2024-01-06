package api.airportservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AirportController {

    @GetMapping("/airport")
    public String getAirport() {
        return "Hello, Airport!";
    }
}
