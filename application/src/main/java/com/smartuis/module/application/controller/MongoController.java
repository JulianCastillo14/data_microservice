package com.smartuis.module.application.controller;

import com.smartuis.module.domian.entity.Message;
import com.smartuis.module.persistence.repository.MongoDBRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

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

    @GetMapping("/measurement/last")
    public ResponseEntity<List<Message>> findLastMeasurements(@RequestParam String value, @RequestParam(required = false, defaultValue = "20") Integer limit){
        List<Message> messages = mongoDBRepository.findLastMeasurements(value, limit);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/measurement/range")
    public ResponseEntity<List<Message>> findMeasurementsByTimeRange(@RequestParam String value, @RequestParam String start, @RequestParam String end){
        Instant fromDate = Instant.parse(start + "T00:00:00Z");
        Instant toDate = Instant.parse(end + "T23:59:59Z");
        List<Message> messages = mongoDBRepository.findMeasurementsByTimeRange(value, fromDate, toDate);
        return ResponseEntity.ok(messages);
    }
}
