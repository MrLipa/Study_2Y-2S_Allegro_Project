package api.flightservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FlightController {

    @GetMapping("/flight")
    public String getFlight() {
        return "Hello, Flight!";
    }
}
