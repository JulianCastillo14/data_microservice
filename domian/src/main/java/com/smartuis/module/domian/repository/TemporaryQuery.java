package com.smartuis.module.domian.repository;

import com.smartuis.module.domian.entity.Message;

import java.time.Instant;
import java.util.List;

public interface TemporaryQuery {

    List<Message> findMessagesBetweenTwoDate(Instant from, Instant to);
    List<Message> findMessagesInUnitsTime(String time);
    List<Message> findLastMeasurements(String measurement, int limit);
    List<Message> findMeasurementsByTimeRange(String measurement, Instant start, Instant end);


}
