package com.smartuis.module.domian.repository;

import com.smartuis.module.domian.entity.Message;

import java.time.Instant;
import java.util.List;

public interface MessageRepository {
    Message write(Message message);
    List<Message> findMessagesByDeviceId(String deviceId);
    List<Message>  findMessagesByLocation(String location);
    List<Message>  findMessagesBetweenTwoDate(Instant from, Instant to);
    List<Message> findMessagesInUnitsTime(String time);
    List<Message> findMessagesForMetric(String metric, Integer limit);
}
