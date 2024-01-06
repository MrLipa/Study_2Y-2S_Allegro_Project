package api.notificationservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

    @GetMapping("/notification")
    public String getNotification() {
        return "Hello, Notification!";
    }
}
