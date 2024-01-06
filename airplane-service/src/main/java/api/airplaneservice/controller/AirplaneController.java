package api.airplaneservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AirplaneController {

    @GetMapping("/airplane")
    public String getAirplane() {
        return "Hello, Airplane!";
    }
}
