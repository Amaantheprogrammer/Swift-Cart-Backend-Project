package com.myProject.health;

import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor
public class HealthController {
    @GetMapping("/")
    public String healthCheck() {
        return "Swift Cart Backend Application is running";
    }
}
