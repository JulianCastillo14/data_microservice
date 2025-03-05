package com.smartuis.module.domian.repository;

import com.smartuis.module.domian.entity.Message;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    Message write(Message message);
}
