package com.smartuis.module.application.controller;

import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.smartuis.module.domian.entity.*;
import com.smartuis.module.persistence.repository.InfluxRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/influx")
public class InfluxController {

    private InfluxRepository influxRepository;

    public InfluxController(InfluxRepository influxRepository) {
        this.influxRepository = influxRepository;
    }

    @GetMapping("/measurement/{measurement}/last")
    public ResponseTemporaryQuery getLastMeasurements(
            @PathVariable String measurement,
            @RequestParam(defaultValue = "10") int limit) {
        List<Message> messages = influxRepository.findLastMeasurements(measurement, limit);
        return transformMessagesToResponse(messages);
    }

    @GetMapping("/by-time-range/measurement/{measurement}")
    public ResponseTemporaryQuery getMeasurementsByTimeRangeMeasurement(
            @PathVariable String measurement,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME ) Instant end){
        List<Message> messages = influxRepository.findMeasurementsByTimeRange(measurement, start, end);
        return transformMessagesToResponseRange(messages);
    }

    @GetMapping("/by-time-range")
    public ResponseTemporaryQuery getMeasurementsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME ) Instant end){
        List<Message> messages = influxRepository.findMessagesBetweenTwoDate(start, end);
        return transformMessagesToResponseRange(messages);
    }

    @GetMapping("/date/units/{time}")
    public ResponseTemporaryQuery getMessagesInUnitsTime(
            @PathVariable String time){

        List<Message> messages = influxRepository.findMessagesInUnitsTime(time);
        return transformMessagesToResponseRange(messages);
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

    public ResponseTemporaryQuery transformMessagesToResponse(List<Message> messages){
        Instant start = messages.get(messages.size() - 1).getHeader().getTimeStamp();
        Instant end = messages.get(0).getHeader().getTimeStamp();

        ResponseTemporaryQuery responseTemporaryQuery = new ResponseTemporaryQuery(start, end);
        List<DataDTO> dataDTO = new ArrayList<>();
        for (Message message : messages) {
            Header header = message.getHeader();
            for (Metric metric : message.getMetrics()) {
                dataDTO.add(new DataDTO(
                        header.getLocation(),
                        metric.getMeasurement(),
                        metric.getValue(),
                        header.getTimeStamp()
                ));
            }
        }
        responseTemporaryQuery.setData(dataDTO);
        return responseTemporaryQuery;
    }

    public ResponseTemporaryQuery transformMessagesToResponseRange(List<Message> messages){
        Instant start = messages.get(0).getHeader().getTimeStamp();
        Instant end = messages.get(messages.size() - 1).getHeader().getTimeStamp();

        ResponseTemporaryQuery responseTemporaryQuery = new ResponseTemporaryQuery(start, end);
        List<DataDTO> dataDTO = new ArrayList<>();
        for (Message message : messages) {
            Header header = message.getHeader();
            for (Metric metric : message.getMetrics()) {
                dataDTO.add(new DataDTO(
                        header.getLocation(),
                        metric.getMeasurement(),
                        metric.getValue(),
                        header.getTimeStamp()
                ));
            }
        }
        responseTemporaryQuery.setData(dataDTO);
        return responseTemporaryQuery;
    }
}