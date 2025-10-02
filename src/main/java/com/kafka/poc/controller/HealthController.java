package com.kafka.poc.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Object> healthCheck() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", "Running");
        return ResponseEntity.status(HttpStatus.OK).body(status);
    }
}
