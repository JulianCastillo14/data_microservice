package com.smartuis.module.application.controller;

import com.smartuis.module.application.mapper.MessageMapper;
import com.smartuis.module.domian.entity.*;
import com.smartuis.module.persistence.repository.InfluxRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/influx")
public class InfluxController {

    private InfluxRepository influxRepository;
    private MessageMapper messageMapper;

    public InfluxController(InfluxRepository influxRepository, MessageMapper messageMapper) {
        this.influxRepository = influxRepository;
        this.messageMapper = messageMapper;
    }

    @GetMapping("/measurement/{measurement}/last")
    public ResponseTemporaryQuery getLastMeasurements(
            @PathVariable String measurement,
            @RequestParam(defaultValue = "10") int limit) {
        List<Message> messages = influxRepository.findLastMeasurements(measurement, limit);
        Instant start = messages.get(messages.size() - 1).getHeader().getTimeStamp();
        Instant end = messages.get(0).getHeader().getTimeStamp();
        List<DataDTO> dataDTOs = messageMapper.mapMessagesToDataDTOs(messages);
        ResponseTemporaryQuery response = new ResponseTemporaryQuery(start, end);
        response.setData(dataDTOs);
        return response;
    }

    @GetMapping("/by-time-range/measurement/{measurement}")
    public ResponseTemporaryQuery getMeasurementsByTimeRangeMeasurement(
            @PathVariable String measurement,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME ) Instant end){
        List<Message> messages = influxRepository.findMeasurementsByTimeRange(measurement, start, end);
        start = messages.get(0).getHeader().getTimeStamp();
        end = messages.get(messages.size() - 1).getHeader().getTimeStamp();
        List<DataDTO> dataDTOs = messageMapper.mapMessagesToDataDTOs(messages);
        ResponseTemporaryQuery response = new ResponseTemporaryQuery(start, end);
        response.setData(dataDTOs);
        return response;
    }

    @GetMapping("/by-time-range")
    public ResponseTemporaryQuery getMeasurementsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME ) Instant end){
        List<Message> messages = influxRepository.findMessagesBetweenTwoDate(start, end);
        start = messages.get(0).getHeader().getTimeStamp();
        end = messages.get(messages.size() - 1).getHeader().getTimeStamp();
        List<DataDTO> dataDTOs = messageMapper.mapMessagesToDataDTOs(messages);
        ResponseTemporaryQuery response = new ResponseTemporaryQuery(start, end);
        response.setData(dataDTOs);
        return response;
    }

    @GetMapping("/date/units/{time}")
    public ResponseTemporaryQuery getMessagesInUnitsTime(
            @PathVariable String time){
        List<Message> messages = influxRepository.findMessagesInUnitsTime(time);
        Instant start = messages.get(0).getHeader().getTimeStamp();
        Instant end = messages.get(messages.size() - 1).getHeader().getTimeStamp();
        List<DataDTO> dataDTOs = messageMapper.mapMessagesToDataDTOs(messages);
        ResponseTemporaryQuery response = new ResponseTemporaryQuery(start, end);
        response.setData(dataDTOs);
        return response;
    }

    @GetMapping("/measurement/{measurement}/average")
    public Optional<Double> getMeasurementAverage(
            @PathVariable String measurement,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME ) Instant end){
        Optional<Double> average = influxRepository.findAverageValue(measurement, start, end);
        return average;
    }

    @GetMapping("/measurement/{measurement}/min")
    public Optional<Double> getMinimum(
            @PathVariable String measurement,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME ) Instant end) {
        Optional<Double> min = influxRepository.findMinValue(measurement, start, end);
        return min;
    }

    @GetMapping("/measurement/{measurement}/max")
    public Optional<Double> getMaximum(
            @PathVariable String measurement,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME ) Instant end) {
        Optional<Double> max = influxRepository.findMaxValue(measurement, start, end);
        return max;
    }
}