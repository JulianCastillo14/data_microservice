package com.smartuis.module.persistence.repository;

import com.smartuis.module.domian.entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IMongoRepository extends MongoRepository<Message, String>{
}
