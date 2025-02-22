package com.smartuis.module.application.amqp;


import com.smartuis.module.domian.entity.Message;
import com.smartuis.module.persistence.repository.InfluxRepository;
import org.springframework.stereotype.Component;

@Component
public class RabbitListener {

   InfluxRepository influxRepository;

    public RabbitListener(InfluxRepository influxRepository) {
        this.influxRepository = influxRepository;
    }

    @org.springframework.amqp.rabbit.annotation.RabbitListener(queues = "#{anonQueue.name}")
    public void receiveMessage(Message message){
        System.out.println(message.toString());
        influxRepository.write(message);
    }

}
