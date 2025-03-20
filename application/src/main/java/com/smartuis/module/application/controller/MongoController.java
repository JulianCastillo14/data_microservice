package com.smartuis.module.application.controller;

import com.smartuis.module.domian.entity.Message;
import com.smartuis.module.persistence.repository.MessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/mongo")
public class MongoController {

    private MessageRepository messageRepository;

    public MongoController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @GetMapping("/deviceId/{deviceId}")
    public ResponseEntity<List<Message>> findMessagesByDeviceId(@PathVariable String deviceId){
        System.out.println(deviceId);
        List<Message> messages = messageRepository.findMessagesByDeviceId(deviceId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<List<Message>> findMessagesByLocation(@PathVariable String location){
        System.out.println(location);
        List<Message> messages = messageRepository.findMessagesByLocation(location);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/date")
    public ResponseEntity<List<Message>> findMessagesByLocation(@RequestParam String from, @RequestParam String to){
        Instant fromDate = Instant.parse(from + "T00:00:00Z");
        Instant toDate = Instant.parse(to + "T23:59:59Z");
        System.out.println(from + " " +  to);
        List<Message> messages = messageRepository.findMessagesBetweenTwoDate(fromDate, toDate);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/date/units/{time}")
    public ResponseEntity<List<Message>> findMessageInUnitsTime(@PathVariable String time) {
        return ResponseEntity.ok(messageRepository.findMessagesInUnitsTime(time));
    }

    @GetMapping("/measurement/last")
    public ResponseEntity<List<Message>> findLastMeasurements(@RequestParam String value, @RequestParam(required = false, defaultValue = "20") Integer limit){
        List<Message> messages = messageRepository.findLastMeasurements(value, limit);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/measurement/range")
    public ResponseEntity<List<Message>> findMeasurementsByTimeRange(@RequestParam String value, @RequestParam String start, @RequestParam String end){
        Instant fromDate = Instant.parse(start + "T00:00:00Z");
        Instant toDate = Instant.parse(end + "T23:59:59Z");
        List<Message> messages = messageRepository.findMeasurementsByTimeRange(value, fromDate, toDate);
        return ResponseEntity.ok(messages);
    }
}
