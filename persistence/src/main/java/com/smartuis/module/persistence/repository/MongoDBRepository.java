package com.smartuis.module.persistence.repository;

import com.smartuis.module.domian.entity.Message;
import com.smartuis.module.domian.repository.MessageRepository;
import org.springframework.stereotype.Repository;

@Repository
public class MongoDBRepository implements MessageRepository {

    private IMongoRepository imongoRepository;

    public MongoDBRepository(IMongoRepository imongoRepository) {
        this.imongoRepository = imongoRepository;
    }


    @Override
    public Message write(Message message) {
        return imongoRepository.save(message);
    }
}
