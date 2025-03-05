package com.smartuis.module.domian.repository;

import com.smartuis.module.domian.entity.Message;

import java.util.List;

public interface FilterQuery {

    List<Message> findMessagesByDeviceId(String deviceId);
    List<Message>  findMessagesByLocation(String location);
}
