package com.smartuis.module.application.controller;

import com.smartuis.module.domian.entity.Message;
import com.smartuis.module.persistence.repository.MongoDBRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.Default;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/mongo")
public class MongoController {

    private MongoDBRepository mongoDBRepository;

    public MongoController(MongoDBRepository mongoDBRepository) {
        this.mongoDBRepository = mongoDBRepository;
    }

    @GetMapping("/deviceId/{deviceId}")
    public ResponseEntity<List<Message>> findMessagesByDeviceId(@PathVariable String deviceId){
        System.out.println(deviceId);
        List<Message> messages = mongoDBRepository.findMessagesByDeviceId(deviceId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<List<Message>> findMessagesByLocation(@PathVariable String location){
        System.out.println(location);
        List<Message> messages = mongoDBRepository.findMessagesByLocation(location);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/date")
    public ResponseEntity<List<Message>> findMessagesByLocation(@RequestParam String from, @RequestParam String to){
        Instant fromDate = Instant.parse(from + "T00:00:00Z");
        Instant toDate = Instant.parse(to + "T23:59:59Z");
        System.out.println(from + " " +  to);
        List<Message> messages = mongoDBRepository.findMessagesBetweenTwoDate(fromDate, toDate);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/date/units/{time}")
    public ResponseEntity<List<Message>> findMessageInUnitsTime(@PathVariable String time) {
        return ResponseEntity.ok(mongoDBRepository.findMessagesInUnitsTime(time));
    }

    @GetMapping("/metrics")
    public ResponseEntity<List<Message>> findMessagesForMetric(@RequestParam String value, @RequestParam(required = false, defaultValue = "20") Integer limit){
        List<Message> messages = mongoDBRepository.findMessagesForMetric(value, limit);
        return ResponseEntity.ok(messages);
    }
}
