package com.smartuis.module.persistence.repository;

import com.smartuis.module.domian.entity.Message;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface IMongoRepository extends MongoRepository<Message, String>{

    @Query("{'headers.deviceId': ?0}")
    List<Message> findMessagesByDeviceId(String deviceId);

    @Query("{'headers.location': ?0}")
    List<Message> findMessagesByLocation(String location);

    @Query("{'headers.timeStamp': {$gte: ?0, $lt: ?1}}")
    List<Message> findMessagesBetweenTwoDate(Instant from, Instant to);

    @Aggregation(pipeline = {
            "{ '$match' : { 'metrics': { '$elemMatch': { 'measurement':  { '$regex': ?0, '$options': 'i' } } } } }",
            "{ '$limit' : ?1 }"
    })
    List<Message> findMessagesForMetric(String metric, Integer limit);


}
