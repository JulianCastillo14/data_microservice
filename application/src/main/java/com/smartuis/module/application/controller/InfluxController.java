package com.smartuis.module.application.controller;

import com.smartuis.module.domian.entity.Message;
import com.smartuis.module.persistence.repository.InfluxRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/influx")
public class InfluxController {

    private InfluxRepository influxRepository;

    public InfluxController(InfluxRepository influxRepository) {
        this.influxRepository = influxRepository;
    }

    @GetMapping("/measurement/{measurement}/last")
    public ResponseEntity<List<Message>> getLastMeasurements(
            @PathVariable String measurement,
            @RequestParam(defaultValue = "10") int limit) {
        List<Message> messages = influxRepository.findLastMeasurements(measurement, limit);
        return ResponseEntity.ok(messages);
    }
}
